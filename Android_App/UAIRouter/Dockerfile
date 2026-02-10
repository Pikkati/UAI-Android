# UAI Router Development Environment Dockerfile
FROM reactnativecommunity/react-native-android:latest

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies (include devDependencies so metro config packages are available)
RUN npm install --legacy-peer-deps --no-audit --progress=false

# Copy source code
COPY . .

# Create Android build directory
RUN mkdir -p android/app/src/main/assets

# Expose ports
EXPOSE 8081 8082

# Health check for Metro bundler
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081 || exit 1

# Default command - start Metro bundler
CMD ["npx", "react-native", "start", "--port", "8082", "--host", "0.0.0.0"]
