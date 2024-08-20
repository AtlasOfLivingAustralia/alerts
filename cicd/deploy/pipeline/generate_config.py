import argparse
import yaml
from jinja2 import Environment, FileSystemLoader

# Set up argument parsing
parser = argparse.ArgumentParser(description='Generate config file from template and values file.')
parser.add_argument('template_file', type=str, help='Path to the template file.')
parser.add_argument('values_file', type=str, help='Path to the values file.')
parser.add_argument('--output_file', type=str, default='config.properties', help='Output file name (default: config.properties).')

args = parser.parse_args()

# Load the template
file_loader = FileSystemLoader('.')
env = Environment(loader=file_loader)
template = env.get_template(args.template_file)

# Load the values from the YAML file
with open(args.values_file) as f:
    values = yaml.safe_load(f)

# Render the template with the values
config_output = template.render(values)

# Write the output to the specified file
with open(args.output_file, 'w') as f:
    f.write(config_output)

print(f"Config file generated successfully: {args.output_file}")