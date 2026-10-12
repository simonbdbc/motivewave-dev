// import LoginWallet from "pages/Auth/LoginWallet";
// import Dashboard from "pages/Dashboard";
// import Investment from "pages/Investment";
// import Reward from "pages/Reward";
// import Swap from "pages/Swap";
// import DevTesting from "pages/DevTesting";
// import NoPageFound from "pages/NoPageFound";
// import Terms from "pages/Terms";
// import Settings from "pages/Settings";
// import Tables from "pages/Tables";
// import Settings from "pages/Settings";
import { lazy } from "react";
import { useRoutes } from "react-router-dom";
import Detection from "pages/Detection";
import LogStrategy from "pages/LogStrategy";
import LogDebug from "pages/LogDebug";
// import Admin from "pages/Admin";
import AlgoBacktest from "pages/AlgoBacktest";
import AlgoUpdateStats from "pages/AlgoUpdateStats";
import TradeSession from "pages/TradeSession";
// import Tables from "pages/Tables";
// const Auth = lazy(() => import("layouts/Auth"));
// const LoginWallet = lazy(() => import("pages/Auth/LoginWallet"));
// const Docs = lazy(() => import("layouts/Docs"));
// const Terms = lazy(() => import("pages/Terms"));
const Appli = lazy(() => import("layouts/Appli"));
const Dashboard = lazy(() => import("pages/Dashboard"));
const Admin = lazy(() => import("pages/Admin"));
const NoPageFound = lazy(() => import("pages/NoPageFound"));

const Pages = [
    // {
    //   path: "/auth",
    //   element: <Auth />,
    //   children: [{ index: true, element: <LoginWallet /> }],
    // },
    {
        path: "/",
        element: <Appli />,
        children: [
            { index: true, element: <Dashboard /> },
            { path: "/trade-session", element: <TradeSession /> },
            { path: "/algo-backtest", element: <AlgoBacktest /> },
            { path: "/algo-update-stats", element: <AlgoUpdateStats /> },
            { path: "/log/strategy", element: <LogStrategy /> },
            { path: "/log/motivewave", element: <LogDebug /> },
            { path: "/notification", element: <Detection /> },
            { path: "/admin", element: <Admin /> },
            // { path: "/tables", element: <Tables /> },
            // { path: "/investment", element: <Investment /> },
            // { path: "/reward", element: <Reward /> },
            // { path: "/swap", element: <Swap /> },
            // { path: "/testing", element: <DevTesting /> },
            // { path: "/settings", element: <Settings /> },
        ],
    },
    // {
    //     path: "/docs",
    //     element: <Docs />,
    //     children: [{ path: "/docs/terms", element: <Terms /> }],
    // },
    // { path: "/wallet", element: <LoginWallet /> },
    { path: "*", element: <NoPageFound /> },
];

const MenuAppli = [
    { name: "Dashboard", path: "/" },
    { name: "Trade Session", path: "/trade-session" },
    { name: "Algo Backtest", path: "/algo-backtest" },
    { name: "Update Stats", path: "/algo-update-stats" },
    { name: "Log Strategy", path: "/log/strategy" },
    { name: "Log Debug", path: "/log/motivewave" },
    { name: "Notification", path: "/notification" },
    { name: "Admin", path: "/admin" },
    // { name: "Tables Example", path: "/tables" },
    // { name: "Investment", path: "/investment" },
    // { name: "Reward", path: "/reward" },
    // { name: "Buy & Sell", path: "/swap" },
    // { name: "Dev Testing", path: "/testing" },
    // { name: "Settings", path: "/settings" },
    // { name: "Aller sur le site", path: "/auth" },
];

const MenuFooter = [
    { name: "Dashboard", path: "/" },
    // { name: "Trade Session", path: "/trade-session" },
    // { name: "Algo Backtest", path: "/algo-backtest" },
    // { name: "Update Stats", path: "/algo-update-stats" },
    // { name: "Log Strategy", path: "/log/strategy" },
    // { name: "Log Debug", path: "/log/motivewave" },
    // { name: "Notification", path: "/notification" },
    { name: "Admin", path: "/admin" },
    // { name: "Terms", path: "/docs/terms" }
];

const Routes = () => {
    return useRoutes(Pages);
};

export default Routes;
export { MenuAppli, MenuFooter };
