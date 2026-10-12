// import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
// import "./index.css";
import "assets/css/tailwind.css";
import App from "./App.tsx";
import { EvmGlobalContextProvider } from "context/EvmGlobalContext";
import { LoaderContextProvider } from "context/LoaderContext";
import HotToast from "components/HotToast";

createRoot(document.getElementById("root")!).render(
    // <StrictMode>
    <EvmGlobalContextProvider>
        <HotToast />
        <LoaderContextProvider>
            <App />
        </LoaderContextProvider>
    </EvmGlobalContextProvider>
    // </StrictMode>,
);
