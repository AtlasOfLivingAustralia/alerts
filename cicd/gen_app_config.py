#!/usr/bin/env python3

import argparse
import os
import jinja2
from botocore.exceptions import ClientError

def parse_values_file(lines):
    values = {}
    for line in lines:
        line = line.strip()
        if line and not line.startswith('#') and '=' in line:
            key, value = line.split('=', 1)
            values[key.strip()] = value.strip()
    return values

def main():
    parser = argparse.ArgumentParser(description='Generate config file using Jinja2 template.')
    parser.add_argument('--template', '-t', required=True, help='Path to the Jinja2 template file')
    parser.add_argument('--values', '-v', required=True, help='Path to the values file')
    parser.add_argument('--branch', '-b', required=True, help='Branch name to substitute in the template')
    parser.add_argument('--output_file', '-o', required=True, help='Output file name')
    args = parser.parse_args()

    # Load the values file and replace "branchName" with the actual branch name, this is specific to the alerts app based on the file set up in ansible-inventories
    with open(args.values) as f:
        values_content = f.read()
    values_content = values_content.replace("branchName", args.branch)

    # Parse the values
    values = parse_values_file(values_content.splitlines())

    # Load the Jinja2 template
    template_loader = jinja2.FileSystemLoader(searchpath=os.path.dirname(args.template))
    template_env = jinja2.Environment(loader=template_loader)
    template = template_env.get_template(os.path.basename(args.template))

    # Render the template with the values
    output_text = template.render(values)

    # Write the output to the output file
    with open(output_file, 'w') as output_file:
        output_file.write(output_text)

if __name__ == '__main__':
    main()