import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  turbopack: {
    // Prevent Next from inferring a wrong monorepo root on Windows
    // when multiple lockfiles exist on disk.
    root: __dirname,
  },
};

export default nextConfig;
