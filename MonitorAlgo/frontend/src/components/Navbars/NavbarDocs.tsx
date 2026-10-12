import React from "react";

import { Link } from "react-router-dom";
import { LogoTransparentIcon, MenuIcon, CloseIcon } from "icons";

const NavbarDocs = () => {
  const [navbarOpen, setNavbarOpen] = React.useState(false);
  return (
    <>
      <nav className="top-0 absolute z-50 w-full flex flex-wrap items-center justify-between py-3 navbar-expand-lg">
        <div className="w-full md:container mx-auto flex flex-wrap items-center justify-between">
          <div className="w-full relative px-6 flex justify-between md:w-auto md:static md:block md:justify-start">
            <Link
              className="text-white flex items-center text-sm font-bold leading-relaxed mr-4 py-2 whitespace-nowrap uppercase"
              to="/"
            >
              <LogoTransparentIcon className="inline mr-2 w-6 h-6" aria-hidden="true" />
              Ecudor Token
            </Link>
            <button
              className="cursor-pointer text-xl leading-none px-3 py-1 border border-solid border-transparent rounded bg-transparent block md:hidden outline-none focus:outline-none"
              type="button"
              onClick={() => setNavbarOpen(!navbarOpen)}
            >
              <MenuIcon className="w-6 h-6 text-white" aria-hidden="true" />
            </button>
          </div>
          <div
            className={
              "md:flex flex-grow items-center bg-white px-6 md:bg-opacity-0 md:shadow-none" +
              (navbarOpen ? " block rounded-b shadow-lg" : " hidden")
            }
            id="example-navbar-warning"
          >
            <ul className="flex flex-col md:flex-row list-none md:ml-auto">
              <Link key={"/"} to={"/"}>
                <li>
                  <span className="text-white hover:text-primary font-semibold block pb-2 text-sm">
                    <CloseIcon className="inline w-6 h-6" aria-hidden="true"/>
                  </span>
                </li>
              </Link>
            </ul>
          </div>
        </div>
      </nav>
    </>
  );
};

export default NavbarDocs;
