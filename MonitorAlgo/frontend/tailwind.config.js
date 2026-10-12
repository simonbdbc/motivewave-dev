import colors from "tailwindcss/colors";

delete colors["lightBlue"];
delete colors["warmGray"];
delete colors["trueGray"];
delete colors["coolGray"];
delete colors["blueGray"];

export const content = ["./src/**/*.{html,js,ts,tsx}"];
export const theme = {
    colors: {
        ...colors,
        transparent: "transparent",
        current: "currentColor",

        primary: "#FFD238",
        "primary-light": "#FFDB5C",
        "primary-dark": "#ECB908",

        secondary: "#77bde1",
        "secondary-light": "#b9d7e9",
        "secondary-dark": "#0f5590",
        // 'secondary': '#281BA4',
        // 'secondary-light': '#453AB1',
        // 'secondary-dark': '#1C127E',
        tertiary: "#7E2CAA",
        "tertiary-light": "#944ABC",
        "tertiary-dark": "#6B0E9E",

        "gray-app": "#4b5563",
        "black-app": "#232323",
    },
    extend: {},
};
export const plugins = [];
