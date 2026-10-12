// import React, { useState } from "react";

// components

const CardDevTesting = ({ TestingClaimMockJEUR }: any) => {
    const ClaimJEURHandler = () => {
        const claimJEUR = async () => {
            await TestingClaimMockJEUR();
        };
        claimJEUR().catch(console.error);
    };

    return (
        <>
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-slate-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center flex justify-between">
                        <h6 className="text-slate-700 text-xl font-bold">
                            FOR TESTING PURPOSE ONLY
                        </h6>
                    </div>
                </div>
                <div className="flex-auto px-4 lg:px-10 py-10 pt-0">
                    <form>
                        <div className="flex flex-wrap mt-10 mb-6">
                            {/* https://faucet.polygon.technology/ */}
                            {/* <div className="w-full lg:w-6/12 px-4">
                <div className="relative w-full mb-3">
                  <label
                    className="block uppercase text-slate-400 text-xs font-bold mb-2"
                    htmlFor="grid-password"
                  >
                    Contract Mock JEUR Balance
                  </label>
                  <input
                    disabled
                    type="text"
                    className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none w-full hover:none"
                    value={jeurContractBalanceJEUR}
                  />
                </div>
              </div>
              <div className="w-full lg:w-6/12 px-4">
                <div className="relative w-full mb-3">
                  <label
                    className="block uppercase text-slate-400 text-xs font-bold mb-2"
                    htmlFor="grid-password"
                  >
                    My JEUR Reward Available
                  </label>
                  <input
                    disabled
                    type="text"
                    className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                    value={0}
                  />
                </div>
              </div> */}
                        </div>

                        <hr className="mt-6 border-b-1 border-slate-300" />

                        <div className="flex flex-wrap mt-10 mb-6">
                            <div className="w-full lg:w-12/12 px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="grid-password"
                                    >
                                        Claim Mock jEUR
                                    </label>
                                </div>
                            </div>
                            <div className="w-full lg:w-12/12 px-4">
                                <div className="relative w-full mb-3">
                                    <div className=" flex justify-between items-center">
                                        <button
                                            type="button"
                                            className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                                            onClick={() => ClaimJEURHandler()}
                                        >
                                            Claim jEUR
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="flex-auto mb-0 px-4 py-6 pt-0">
                            <div className="text-slate-400 text-center mb-6 font-bold">
                                <small>
                                    If you need some Matic for gas fee :{" "}
                                    <a
                                        href="https://faucet.polygon.technology/"
                                        target="_blank"
                                        rel="noopener noreferrer"
                                    >
                                        Link to Polygon Mumbai Faucet
                                    </a>
                                </small>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </>
    );
};
export default CardDevTesting;
