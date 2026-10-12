// import React from "react";

const FooterAuth = () => {
  return (
    <footer className="w-full bg-slate-800 pt-8 pb-4">
      <div className="container mx-auto px-4">
        <hr className="my-4 border-zinc-400" />
        <div className="flex flex-wrap items-center lg:justify-between justify-center">
          <div className="w-full md:w-4/12 px-2 mx-auto text-center">
            <div className="text-xs text-white font-semibold py-1">
              Copyright © {new Date().getFullYear()} Ecudor Token by SB BLOCK.
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default FooterAuth;
