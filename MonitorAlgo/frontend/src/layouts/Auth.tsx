// import React from "react";
import { Navigate, Outlet } from "react-router-dom";

import Navbar from "components/Navbars/NavbarAuth";
import useEvmGlobalContext from "context/EvmGlobalContext";

export default function Auth() {
  const { walletState }:any = useEvmGlobalContext();
  const {
    account: { isConnected },
  } = walletState;

  if (isConnected) {
    return <Navigate to="/" replace />;
  }

  return (
    <>
      <Navbar />
      <main>
        <Outlet />
      </main>
    </>
  );
}
