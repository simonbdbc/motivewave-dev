// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import svgr from "vite-plugin-svgr";

import path from "path";

// https://vite.dev/config/
export default defineConfig({
    plugins: [react(), svgr()],
    resolve: {
        alias: {
            assets: path.resolve(__dirname, "src/assets"),
            components: path.resolve(__dirname, "src/components"),
            constants: path.resolve(__dirname, "src/constants"),
            context: path.resolve(__dirname, "src/context"),
            data: path.resolve(__dirname, "src/data"),
            hooks: path.resolve(__dirname, "src/hooks"),
            icons: path.resolve(__dirname, "src/icons"),
            interfaces: path.resolve(__dirname, "src/interfaces"),
            layouts: path.resolve(__dirname, "src/layouts"),
            pages: path.resolve(__dirname, "src/pages"),
            router: path.resolve(__dirname, "src/router"),
            // ajoute d'autres alias si besoin
        },
    },
    server: {
        proxy: {
            "/api": "http://localhost:8080",
            "/ws-logfile": {
                target: "ws://localhost:8080",
                ws: true,
            },
        },
    },
});
