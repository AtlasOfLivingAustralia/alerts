FROM ubuntu:26.04

# Install system dependencies and Python tooling
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        bubblewrap \
        build-essential \
        curl \
        git \
        jq \
        locales \
        openssh-client \
        python3 \
        python3-pip \
        python3-venv \
        ruby-full \
        socat \
        sudo \
        unzip \
        vim \
        zip \
    && locale-gen en_US.UTF-8 \
    && update-locale LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Ensure ubuntu user can sudo without a password
RUN echo "ubuntu ALL=(ALL:ALL) NOPASSWD:ALL" > /etc/sudoers.d/ubuntu \
    && chmod 0440 /etc/sudoers.d/ubuntu

# Install cfn-nag (Ruby gem)
RUN gem install cfn-nag --no-document

# Install AWS rain
RUN ARCH=$(dpkg --print-architecture) \
    && LATEST="$(curl -s https://api.github.com/repos/aws-cloudformation/rain/releases/latest | jq -r '.tag_name')" \
    && curl -L -o rain.zip "https://github.com/aws-cloudformation/rain/releases/download/${LATEST}/rain-${LATEST}_linux-${ARCH}.zip" \
    && unzip rain.zip \
    && mv "rain-${LATEST}_linux-${ARCH}/rain" /usr/local/bin/ \
    && rm rain.zip

# Install Python tools inside a virtual environment
RUN python3 -m venv /opt/pyenv \
    && /opt/pyenv/bin/pip install --upgrade pip setuptools wheel --no-cache-dir \
    && /opt/pyenv/bin/pip install --no-cache-dir \
        cfn-lint \
        boto3 \
        awscli \
        pre-commit \
        pre-commit-hooks \
        black \
        pydot \
        pyyaml

# Make the virtual environment the default Python and set locale
ENV PATH="/opt/pyenv/bin:$PATH" \
    LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    LANGUAGE=en_US:en

USER ubuntu
