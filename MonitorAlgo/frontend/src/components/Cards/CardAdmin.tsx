import { useState } from "react";

// components

const CardAdmin = ({
    ecudorOwnerAddress,
    investmentContractBalanceECOR,
    transferOwnership,
}: any) => {
    const [openValidationOwner, setOpenValidationOwner] = useState(false);
    const [inputValueOwner, setInputValueOwner] = useState("");

    const inputChangeValueOwner = (event: any) => {
        event.preventDefault();
        setInputValueOwner(event.target.value);
        // console.log(event.target.value);
    };
    // const buyTokenHandler = () => {
    //   const buyToken = async () => {
    //     await buyEcudorToken(inputValueBuy);
    //   };
    //   buyToken().catch(console.error);
    // };

    const TransferOwnershipHandler = async () => {
        // const TransferOwnership = async () => {
        try {
            await transferOwnership(inputValueOwner);
        } catch (error) {
            console.error(error);
        }
        // };
        // TransferOwnership().catch(console.error);
    };

    return (
        <>
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-slate-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center flex justify-between">
                        <h6 className="text-slate-700 text-xl font-bold">
                            Admin
                        </h6>
                    </div>
                </div>
                <div className="flex-auto px-4 lg:px-10 py-10 pt-0">
                    <form>
                        <div className="flex flex-wrap mt-10 mb-6">
                            <div className="w-full px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="admin-contract-owner-address"
                                    >
                                        Contract Owner Address
                                    </label>
                                    <input
                                        disabled
                                        type="text"
                                        id="admin-contract-owner-address"
                                        className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none w-full hover:none"
                                        value={ecudorOwnerAddress}
                                    />
                                </div>
                            </div>
                            <div className="w-full lg:w-6/12 px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="admin-contract-balance-total"
                                    >
                                        Contract Balance Total
                                    </label>
                                    <input
                                        disabled
                                        type="text"
                                        id="admin-contract-balance-total"
                                        className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                        value={investmentContractBalanceECOR}
                                    />
                                </div>
                            </div>
                            <div className="w-full lg:w-6/12 px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="admin-contract-balance-batch"
                                    >
                                        Contract Balance Batch
                                    </label>
                                    <input
                                        disabled
                                        type="text"
                                        id="admin-contract-balance-batch"
                                        className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                        value={investmentContractBalanceECOR}
                                    />
                                </div>
                            </div>
                        </div>

                        <hr className="mt-6 border-b-1 border-slate-300" />

                        <div className="flex flex-wrap mt-10 mb-6">
                            <div className="w-full lg:w-12/12 px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="admin-transfer-ownership"
                                    >
                                        Transfer Ownership
                                    </label>
                                </div>

                                <div className="relative w-full mb-3 p-2 bg-white border shadow rounded">
                                    <div className="flex justify-between items-center">
                                        <input
                                            type="text"
                                            id="admin-transfer-ownership"
                                            className={
                                                openValidationOwner
                                                    ? "hidden"
                                                    : "border-0 px-3 py-3 h-10 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                            }
                                            // className="border-0 px-3 py-3 mr-2 h-10 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                            onChange={inputChangeValueOwner}
                                            value={inputValueOwner}
                                        />

                                        <div
                                            className={
                                                openValidationOwner
                                                    ? "ml-2"
                                                    : "hidden"
                                            }
                                        >
                                            {inputValueOwner}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="w-full lg:w-12/12 px-4">
                                <div className="relative w-full mb-3">
                                    <div
                                        className={
                                            openValidationOwner
                                                ? " hidden"
                                                : " flex justify-between items-center"
                                        }
                                    >
                                        <button
                                            type="button"
                                            className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                                            onClick={() => {
                                                inputValueOwner !== "" &&
                                                    setOpenValidationOwner(
                                                        !openValidationOwner
                                                    );
                                            }}
                                        >
                                            Send
                                        </button>
                                    </div>

                                    {/* <!-- container after clicked "EDIT" --> */}
                                    <div
                                        className={
                                            openValidationOwner
                                                ? " flex justify-between items-center"
                                                : " hidden"
                                        }
                                    >
                                        <div className="flex w-full justify-center items-center space-x-2">
                                            <div className="w-6/12">
                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        setOpenValidationOwner(
                                                            false
                                                        )
                                                    }
                                                    className="btn w-full bg-gray-200 hover:bg-gray-300 px-4 py-2 font-medium rounded"
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                            <div className="w-6/12">
                                                <button
                                                    type="button"
                                                    onClick={
                                                        TransferOwnershipHandler
                                                    }
                                                    className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                                                >
                                                    Confirm
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <hr className="mt-6 border-b-1 border-slate-300" />

                        {/* <div className="flex flex-wrap mt-10 mb-6">
              <div className="w-full lg:w-12/12 px-4">
                <div className="relative w-full mb-3">
                  <label
                    className="block uppercase text-slate-400 text-xs font-bold mb-2"
                    htmlFor="grid-password"
                  >
                    Set Approval Amount Manually
                  </label>
                </div>

                <div className="relative w-full mb-3 p-2 bg-white border shadow rounded">
                  <div className="flex justify-between items-center">
                    <input
                      type="number"
                      className={
                        openValidationAllowance
                          ? "hidden"
                          : "border-0 px-3 py-3 mr-2 h-10 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                      }
                      min="0"
                      step="1"
                      onChange={inputChangeValueAllowance}
                      value={inputValueAllowance}
                    />

                    <div className={openValidationAllowance ? "ml-2" : "hidden"}>
                      {inputValueAllowance}
                    </div>
                    <button
                      disabled
                      type="button"
                      className="btn bg-secondary text-white px-4 py-2 font-medium rounded"
                    >
                      jEUR
                    </button>
                  </div>
                </div>
              </div>
              <div className="w-full lg:w-12/12 px-4">
                <div className="relative w-full mb-3">
                  <div
                    className={
                      openValidationAllowance
                        ? " hidden"
                        : " flex justify-between items-center"
                    }
                  >
                    <button
                      type="button"
                      className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                      onClick={() => {
                        inputValueAllowance > 0 && setOpenValidationAllowance(!openValidationAllowance);
                      }}
                    >
                      Set approval
                    </button>
                  </div>

                  <div
                    className={
                      openValidationAllowance
                        ? " flex justify-between items-center"
                        : " hidden"
                    }
                  >
                    <div className="flex w-full justify-center items-center space-x-2">
                      <div className="w-6/12">
                        <button
                          type="button"
                          onClick={() => setOpenValidationAllowance(false)}
                          className="btn w-full bg-gray-200 hover:bg-gray-300 px-4 py-2 font-medium rounded"
                        >
                          Cancel
                        </button>
                      </div>
                      <div className="w-6/12">
                        <button
                          type="button"
                          onClick={setAllowanceHandler}
                          className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                        >
                          Confirm
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div> */}
                    </form>
                </div>
            </div>
        </>
    );
};
export default CardAdmin;
