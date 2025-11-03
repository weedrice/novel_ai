FROM node:20.11.1-alpine

WORKDIR /app

# Set UTF-8 encoding
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

# Copy package files
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm ci || npm install

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Expose port
EXPOSE 3000

# Start the application
CMD ["npm", "run", "start"]
