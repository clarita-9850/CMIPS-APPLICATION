import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  // Ensure all routes are properly handled
  output: 'standalone', // Required for Docker optimization
  experimental: {
    // Disable any route optimization that might cause issues
  },
};

export default nextConfig;
