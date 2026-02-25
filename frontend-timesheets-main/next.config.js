// Always use localhost:8090 for local development (browser cannot resolve Docker hostnames)
// For production/Docker, use NEXT_PUBLIC_API_URL environment variable
const API_PROXY_TARGET = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090';

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone',

  // Proxy API requests to API Gateway (running on localhost:8090)
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${API_PROXY_TARGET}/api/:path*`,
      },
    ];
  },

  // Allow images from external sources if needed
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: '**',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
      },
    ],
  },

  // Webpack configuration for CA.gov CSS
  webpack: (config) => {
    config.resolve.alias = {
      ...config.resolve.alias,
    };
    return config;
  },
};

module.exports = nextConfig;

