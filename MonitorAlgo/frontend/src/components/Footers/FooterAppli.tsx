import React from "react";
import { Link } from "react-router-dom";
import { MenuFooter } from "router/Routes";

const FooterAppli: React.FC<any> = (props) => {
    const { setPageActive } = props;

    return (
        <>
            <footer className="block py-4 px-4 md:px-10 absolute right-0 bottom-0 left-0">
                <div className="container mx-auto px-4">
                    <hr className="mb-4 border-b-1 border-slate-200" />
                    <div className="flex flex-wrap items-center md:justify-between justify-center">
                        <div className="w-full md:w-6/12 px-4">
                            <div className="text-sm text-slate-500 font-semibold py-1 text-center md:text-left">
                                Copyright © {new Date().getFullYear()} Monitor Algo by
                                SB DEV.
                            </div>
                        </div>
                        <div className="w-full md:w-6/12 px-4">
                            <ul className="flex flex-wrap list-none md:justify-end  justify-center">
                                {MenuFooter.map((page) => (
                                    <Link
                                        key={page.path}
                                        to={page.path}
                                        onClick={() => {
                                            setPageActive(page.name);
                                        }}
                                    >
                                        <li>
                                            <span className="text-slate-600 hover:text-slate-800 text-sm font-semibold block py-1 px-3">
                                                {page.name}
                                            </span>
                                        </li>
                                    </Link>
                                ))}
                            </ul>
                        </div>
                    </div>
                </div>
            </footer>
        </>
    );
};

export default FooterAppli;
