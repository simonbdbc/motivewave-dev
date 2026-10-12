/*eslint-disable*/
import React, { useState } from "react";
import { Link, NavLink } from "react-router-dom";

import { LogoOrIcon, MenuIcon } from "icons";
// import useEvmGlobalContext from "context/EvmGlobalContext";
import { MenuAppli } from "router/Routes";
// import useHelper from "hooks/useHelper";
// import DisconnectAction from "context/Actions/Wallet/DisconnectAction";

const SidebarAppli: React.FC<any> = (props) => {
    const { pageActive, setPageActive } = props;
    const [collapseShow, setCollapseShow] = useState(false);
    // const { minifyDisplayAccount } = useHelper();
    // const { walletState, walletDispatch }: any = useEvmGlobalContext();
    // const {
    //     account: { address },
    // } = walletState;

    return (
        <>
            <nav className="md:left-0 md:block md:fixed md:top-0 md:bottom-0 md:overflow-y-auto md:flex-row md:flex-nowrap md:overflow-hidden shadow-xl bg-white flex flex-wrap items-center justify-between relative md:w-64 z-10 py-4 px-6">
                <div className="md:flex-col md:items-stretch md:min-h-full md:flex-nowrap px-0 flex flex-wrap items-center justify-between w-full mx-auto">
                    {/* Brand */}
                    <Link
                        className="flex items-center text-left text-slate-600 mr-0 whitespace-nowrap text-sm uppercase font-bold py-2 px-0"
                        to="/"
                    >
                        <LogoOrIcon
                            className="inline mr-2 w-6 h-6"
                            aria-hidden="true"
                        />
                        Monitor Algo
                    </Link>
                    <h2 className="text-black-app text-sm uppercase inline-block md:hidden font-semibold">
                        {pageActive}
                    </h2>
                    {/* Toggler */}
                    <button
                        className="cursor-pointer text-black opacity-50 md:hidden px-3 py-1 text-xl leading-none bg-transparent rounded border border-solid border-transparent"
                        type="button"
                        onClick={() => setCollapseShow(!collapseShow)}
                    >
                        <MenuIcon className="w-6 h-6" aria-hidden="true" />
                    </button>
                    <div
                        className={
                            "md:flex md:flex-col md:items-stretch md:opacity-100 md:relative md:shadow-none shadow absolute top-0 left-0 right-0 z-40 overflow-y-auto overflow-x-hidden h-auto items-center flex-1 rounded " +
                            (collapseShow
                                ? " bg-white mt-12 block rounded-b shadow-lg pb-4 pt-2 px-6"
                                : " hidden")
                        }
                    >
                        {/* Divider */}
                        <hr className="my-4 md:min-w-full" />
                        {/* <h6 className="md:min-w-full text-slate-500 text-xs uppercase font-bold block pt-1 pb-1 no-underline">
                            Account : {minifyDisplayAccount(address)}
                        </h6> */}
                        {/* Divider */}
                        {/* <hr className="my-4 md:min-w-full" /> */}
                        {/* Navigation */}
                        <ul className="md:flex-col md:min-w-full flex flex-col list-none">
                            {MenuAppli.map((page) => (
                                <li key={page.path} className="items-center">
                                    <NavLink
                                        className={({ isActive }) =>
                                            isActive
                                                ? "text-xs uppercase py-3 font-bold block text-secondary hover:text-secondary-dark"
                                                : "text-xs uppercase py-3 font-bold block text-slate-700 hover:text-slate-500"
                                        }
                                        to={page.path}
                                        onClick={() => {
                                            setPageActive(page.name);
                                            setCollapseShow(false);
                                        }}
                                    >
                                        {page.name}
                                    </NavLink>
                                </li>
                            ))}
                        </ul>
                        {/* Divider */}
                        <hr className="my-4 md:min-w-full" />
                        {/* Navigation */}
                        {/* <ul className="md:flex-col md:min-w-full flex flex-col list-none md:mb-4">
                            <li className="items-center">
                                <a
                                    className="text-slate-700 hover:text-slate-500 text-xs uppercase py-3 font-bold block"
                                    // href={process.env.REACT_APP_URL_SITE}
                                    href={import.meta.env.VITE_REACT_APP_URL_SITE}
                                >
                                    Go to website
                                </a>
                            </li>
                            <li className="items-center">
                                <Link
                                    className="text-slate-700 hover:text-slate-500 text-xs uppercase py-3 font-bold block"
                                    to="/"
                                    onClick={() =>
                                        DisconnectAction(walletDispatch)
                                    }
                                >
                                    Logout
                                </Link>
                            </li>
                        </ul> */}
                    </div>
                </div>
            </nav>
        </>
    );
};

export default SidebarAppli;
