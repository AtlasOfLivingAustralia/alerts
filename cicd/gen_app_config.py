#!/usr/bin/env python3

import argparse
import os
import jinja2

def parse_values_file(values_file_path):
    values = {}
    with open(values_file_path, 'r') as file:
        for line in file:
            line = line.strip()
            if line and not line.startswith('#'):
                key, value = line.split('=', 1)
                values[key.strip()] = value.strip()
    return values

def main():
    parser = argparse.ArgumentParser(description='Generate config file using Jinja2 template.')
    parser.add_argument('--template', '-t', required=True, help='Path to the Jinja2 template file')
    parser.add_argument('--values', '-v', required=True, help='Path to the values file')
    args = parser.parse_args()

    # Load the Jinja2 template
    template_loader = jinja2.FileSystemLoader(searchpath=os.path.dirname(args.template))
    template_env = jinja2.Environment(loader=template_loader)
    template = template_env.get_template(os.path.basename(args.template))

    # Parse the values file
    values = parse_values_file(args.values)

    # Render the template with the values
    output_text = template.render(values)

    # Write the output to alerts-config.properties
    with open('alerts-config.properties', 'w') as output_file:
        output_file.write(output_text)

if __name__ == '__main__':
    main()