/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{js,jsx,ts,tsx}", "./src/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  // "class" (not the default "media") — NativeWind's web runtime throws on its own
  // internal color-scheme sync when darkMode is "media" (see the "Cannot manually set
  // color scheme" error this fixes); "class" works correctly on both web and native.
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        // Matches the existing website's palette (see website/README.md "Design Theme").
        brand: {
          DEFAULT: "#1E40AF",
          accent: "#F97316",
        },
      },
    },
  },
  plugins: [],
};
