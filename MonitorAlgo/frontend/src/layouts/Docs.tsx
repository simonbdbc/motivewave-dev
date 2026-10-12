// import React from "react";
import { Outlet } from "react-router-dom";

import Navbar from "components/Navbars/NavbarDocs";

export default function Docs() {

  return (
    <>
      <Navbar />
      <main>
        <Outlet />
      </main>
    </>
  );
}
