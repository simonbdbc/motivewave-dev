// import React from "react";
// import { Link } from "react-router-dom";
import useEvmGlobalContext from "context/EvmGlobalContext";
// import useEvm from "hooks/useEvm";
import useHelper from "hooks/useHelper";

// import LogoFractalWhite from "assets/img/logo-fractal-white.svg";
// import LogoFractalBlack from "assets/img/logo-fractal-black.svg";
import FooterAuth from "components/Footers/FooterAuth";
import ButtonConnectWallet from "components/Buttons/ButtonConnectWallet";

const LoginWallet = () => {
    // const { connectWallet } = useEvm();
    const { minifyDisplayAccount } = useHelper();
    const { walletState }: any = useEvmGlobalContext();
    const {
        account: { address },
    } = walletState;

    return (
        <>
            <section className="flex flex-col justify-between min-h-screen leading-normal tracking-normal bg-slate-800">
                <div className="pt-24 flex flex-1 items-center">
                    <div className="container mx-auto px-4">
                        <div className="flex flex-wrap justify-center">
                            <div className="w-full lg:w-6/12 px-4">
                                <div className="relative flex flex-col min-w-0 break-words w-full shadow-lg rounded-lg bg-slate-200">
                                    <div className="flex-auto p-5 lg:p-10">
                                        <div className="flex-auto mb-0 px-4 py-6">
                                            <div className="text-slate-400 text-center mb-6 font-bold">
                                                <small>
                                                    Blockchain : Polygon Mumbai
                                                    (testnet)
                                                </small>
                                            </div>
                                            {/* <div className="text-slate-400 text-center mb-6 font-bold">
                        <small>{account}</small>
                        <h6 className="text-slate-500 text-sm font-bold">
                          {address}
                        </h6>
                      </div> */}
                                            <hr className="mt-6 border-b-1 border-slate-300" />
                                        </div>
                                        <div className="flex-auto mb-0 px-4 py-6 pt-0">
                                            {/* <div className="text-center mb-6">
                        <h6 className="text-slate-500 text-sm font-bold">
                          Sign in with
                        </h6>
                      </div> */}
                                            <div className="text-slate-400 text-center mb-6 font-bold">
                                                <small>
                                                    Sign in with MetaMask
                                                </small>
                                            </div>
                                            <div className="flex flex-wrap">
                                                <div className="w-full mb-4">
                                                    <ButtonConnectWallet
                                                        // connectWallet={
                                                        //     // connectWallet
                                                        // }
                                                        displayAccount={minifyDisplayAccount(
                                                            address
                                                        )}
                                                    />
                                                </div>
                                            </div>
                                            <hr className="mt-6 border-b-1 border-slate-300" />
                                        </div>
                                        <div className="flex-auto mb-0 px-4 py-6 pt-0">
                                            <div className="text-slate-400 text-center mb-6 font-bold">
                                                <small>
                                                    <a
                                                        href="https://metamask.io/download.html"
                                                        target="_blank"
                                                        rel="noopener noreferrer"
                                                    >
                                                        Link to MetaMask
                                                        Extension
                                                    </a>
                                                </small>
                                            </div>
                                        </div>
                                        {/* <div className="flex-auto mb-0 px-4 py-6 pt-0">
                      <div className="text-slate-400 text-center mb-6 font-bold">
                        <small>Sign in with</small>
                      </div>
                      <div className="flex flex-wrap">
                        <div className="w-full mb-4">
                          <ButtonConnectWallet
                            connectWallet={connectWallet}
                            displayAccount={minifyDisplayAccount(address)}
                          />
                        </div>
                        <div className="w-full">
                          <Link key="App" to="/">
                            <button
                              className="bg-white active:bg-slate-50 text-slate-700 w-full justify-center px-6 py-3 rounded outline-none focus:outline-none mr-1 mb-1 uppercase shadow hover:shadow-md inline-flex items-center font-bold text-xs ease-linear transition-all duration-150"
                              type="button"
                              // onClick={() => connectWallet()}
                            >
                              <img
                                alt="..."
                                className="inline w-20 mr-3"
                                src={LogoFractalBlack}
                              />
                            </button>
                          </Link>
                        </div>
                      </div>
                      <hr className="mt-6 border-b-1 border-slate-300" />
                    </div> */}
                                        {/* <div className="flex-auto px-4 py-10 pt-0">
                      <div className="text-slate-400 text-center mb-6 font-bold">
                        <small>Or sign up with</small>
                      </div>
                      <div className="btn-wrapper text-center">
                        <button
                          className="bg-secondary text-white active:bg-sky600 text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 w-full ease-linear transition-all duration-150"
                          type="button"
                        >
                          <a
                            href="https://fractal.id/authorize?client_id=a14fc943a00b0711611691da42c956939ff8a9885e38f4a64eb35523ee807873&redirect_uri=https%3A%2F%2Fdemo.fractal.id%2Fapi%2Foauth%2Fcallback&response_type=code&scope=contact:read verification.basic:read verification.basic.details:read verification.liveness:read verification.liveness.details:read"
                            target="_blank"
                            rel="noreferrer"
                          >
                            <img
                              alt="..."
                              className="inline w-20 mr-3"
                              src={LogoFractalWhite}
                            />
                            Go through KYC
                          </a>
                        </button>
                      </div>
                    </div> */}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <FooterAuth />
            </section>
        </>
    );
};

export default LoginWallet;
