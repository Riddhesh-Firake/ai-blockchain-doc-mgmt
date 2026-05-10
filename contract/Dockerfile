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
RUN npm install -g truffle && truffle compile

# The command to run when the container starts
CMD ["node", "scripts/deploy.js"]
