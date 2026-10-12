import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

import NavbarAppli from "components/Navbars/NavbarAppli.js";
import SidebarAppli from "components/Sidebars/SidebarAppli";
import HeaderAppli from "components/Headers/HeaderAppli";
import FooterAppli from "components/Footers/FooterAppli";

// import useEvmGlobalContext from "context/EvmGlobalContext";
import { MenuAppli } from "router/Routes";

const Appli = () => {
    // const { walletState }: any = useEvmGlobalContext();
    // const {
    //     account: { isConnected },
    // } = walletState;

    const [pageActive, setPageActive] = useState("");

    useEffect(() => {
        const MenuActive = MenuAppli.filter((page) => {
            let active = window.location.href.match(
                new RegExp(page.path + "$", "g")
            );
            return active;
        });

        MenuActive[0] && setPageActive(MenuActive[0].name);
    });

    // if (!isConnected) {
    //   return <Navigate to="/auth" replace />;
    // }

    return (
        <>
            <SidebarAppli
                pageActive={pageActive}
                setPageActive={setPageActive}
            />
            <div className="min-h-screen pb-48 relative md:ml-64 bg-slate-100">
                <NavbarAppli pageActive={pageActive} />
                <HeaderAppli />
                <div className="px-4 md:px-10 mx-auto w-full -m-24">
                    <Outlet />
                    <FooterAppli
                        setPageActive={setPageActive}
                    />
                </div>
            </div>
        </>
    );
};

export default Appli;
