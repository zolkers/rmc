#!/usr/bin/env python3
"""
Retrieves protocol.json for Minecraft Java Edition from PrismarineJS/minecraft-data.
Fixes JSON parsing for modern schema structures (mapper/container/switch).

Usage:
    python3 protocol_retriever_v2.py --versions 1.21.4
"""

from __future__ import annotations
import argparse
import json
import time
import pathlib
from typing import Dict, List, Any, Optional

import pandas as pd
import requests

BASE_URL = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/{ver}/protocol.json"
HEADERS = {"User-Agent": "protocol-retriever/clean/2.1"}

DEFAULT_VERSIONS = ["1.21.4"]

def fetch_protocol(version: str, timeout: int = 10) -> Optional[dict]:
    """Fetches protocol.json for a version."""
    url = BASE_URL.format(ver=version)
    try:
        r = requests.get(url, headers=HEADERS, timeout=timeout)
        if r.status_code == 200:
            print(f"INFO: Fetched JSON protocol for {version}")
            return r.json()
        else:
            print(f"WARNING: HTTP {r.status_code} for {version} (URL: {url})")
            return None
    except requests.RequestException as e:
        print(f"ERROR: Connection failed for {version}: {e}")
        return None

def find_packet_mapper(type_def: Any) -> Optional[Dict[str, str]]:
    """
    Recursively searches for the 'mapper' structure which contains the ID->Name mappings.
    Modern protocol.json defines packets like:
    "packet": ["container", [{"name": "name", "type": ["mapper", {"mappings": {...}}] }]]
    """
    if isinstance(type_def, list):
        # Handle ["mapper", {...}]
        if len(type_def) > 1 and type_def[0] == "mapper":
            data = type_def[1]
            if isinstance(data, dict) and "mappings" in data:
                return data["mappings"]

        # Handle ["container", [...fields...]]
        if len(type_def) > 1 and type_def[0] == "container":
            for field in type_def[1]:
                if isinstance(field, dict) and "type" in field:
                    res = find_packet_mapper(field["type"])
                    if res: return res

    return None

def extract_packets(proto: dict) -> List[Dict[str, Any]]:
    """Extract packets from the modern schema structure."""
    rows = []

    # List of protocol states to check
    # Note: 'configuration' was added in 1.20.2+
    states = ["handshaking", "status", "login", "configuration", "play"]

    for state in states:
        if state not in proto:
            continue

        state_obj = proto[state]
        if not isinstance(state_obj, dict):
            continue

        for direction in ("toClient", "toServer"):
            if direction not in state_obj:
                continue

            dir_obj = state_obj[direction]
            if "types" not in dir_obj or "packet" not in dir_obj["types"]:
                continue

            # This is the schema definition of a packet for this state/direction
            packet_def = dir_obj["types"]["packet"]

            # We need to find the ID mappings inside this schema
            mappings = find_packet_mapper(packet_def)

            if mappings:
                for hex_key, name in mappings.items():
                    try:
                        dec_id = int(hex_key, 16)
                    except ValueError:
                        dec_id = None

                    rows.append({
                        "hex_id": hex_key,
                        "dec_id": dec_id,
                        "packet_name": name,
                        "direction": direction,
                        "state": state
                    })
            else:
                # Fallback for very old versions or unexpected structures (flat lists)
                # (Though recent minecraft-data uses mappers)
                pass

    return rows

def main(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", "-o", default="1.21.4_mc_packets.xlsx")
    parser.add_argument("--csv", action="store_true")
    parser.add_argument("--versions", default=",".join(DEFAULT_VERSIONS),
                        help="Comma separated versions (e.g. 1.21.4,1.21.3)")
    parser.add_argument("--timeout", type=int, default=10)
    args = parser.parse_args(argv)

    versions = [v.strip() for v in args.versions.split(",")]
    sheets: Dict[str, pd.DataFrame] = {}

    for ver in versions:
        print(f"INFO: Processing version {ver} ...")
        data = fetch_protocol(ver, timeout=args.timeout)

        if not data:
            print(f"SKIP: Could not retrieve data for {ver}. Note: 1.21.5+ does not exist for Java Edition yet.")
            continue

        rows = extract_packets(data)
        if not rows:
            print(f"WARNING: Protocol JSON found but parsing failed (0 packets found) for {ver}.")
            continue

        print(f"INFO: Found {len(rows)} packets for {ver}")

        df = pd.DataFrame(rows)
        # Sort for better readability
        df = df.sort_values(["state", "direction", "dec_id"], na_position="last")
        sheets[ver] = df

        if args.csv:
            csv_name = f"packets_{ver}.csv"
            df.to_csv(csv_name, index=False)
            print(f"INFO: Saved CSV -> {csv_name}")

        time.sleep(0.5) # Be nice to GitHub

    if sheets:
        out_path = pathlib.Path(args.out)
        with pd.ExcelWriter(out_path, engine="openpyxl") as writer:
            for ver, df in sheets.items():
                # Excel sheet names are limited to 31 chars
                df.to_excel(writer, sheet_name=ver[:31], index=False)
        print(f"SUCCESS: All data saved to: {out_path}")
    else:
        print("ERROR: No data was generated (check version numbers or internet connection).")

if __name__ == "__main__":
    main()