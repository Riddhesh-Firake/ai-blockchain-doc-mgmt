# Use an official Node.js runtime as a parent image
FROM node:18-slim

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the package.json and package-lock.json files
COPY package*.json ./

# Install project dependencies
RUN npm install

# Copy the rest of the contract directory contents
COPY . .

# Compile contracts
# We install truffle globally to compile the contracts
RUN npm install -g truffle && truffle compile

# Arguments that can be passed during the build
ARG PRIVATE_KEY
ARG SEPOLIA_RPC_URL

# Set environment variables
ENV PRIVATE_KEY=$PRIVATE_KEY
ENV SEPOLIA_RPC_URL=$SEPOLIA_RPC_URL

# The command to run when the container starts
CMD ["node", "scripts/deploy.js"]
