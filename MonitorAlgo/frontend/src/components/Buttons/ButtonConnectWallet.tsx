import React from "react";

const ButtonConnectWallet: React.FC<any> = (props) => {
  return (
    <>
      <button
        className="bg-secondary text-white active:bg-sky600 w-full justify-center px-6 py-3 rounded outline-none focus:outline-none uppercase shadow hover:shadow-md inline-flex items-center font-bold text-xs ease-linear transition-all duration-150"
        type="button"
        onClick={() => props.connectWallet()}
      >
        {props.displayAccount || "Connect Wallet"}
      </button>
    </>
  );
};

export default ButtonConnectWallet;
