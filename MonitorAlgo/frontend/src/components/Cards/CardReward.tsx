import { useState } from "react";

// components

const CardReward = () => {
    const [openValidationBuy, setOpenValidationBuy] = useState(false);
    const [inputValueBuy]: [any, any] = useState("0");
    // const [openValidationAllowance, setOpenValidationAllowance] =
    //     useState(false);
    // const [inputValueAllowance, setInputValueAllowance] = useState("0");

    return (
        <>
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-slate-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center flex justify-between">
                        <h6 className="text-slate-700 text-xl font-bold">
                            Collect Rewards
                        </h6>
                    </div>
                </div>
                <div className="flex-auto px-4 lg:px-10 py-10 pt-0">
                    <form>
                        <div className="flex flex-wrap mt-10 mb-6">
                            <div className="w-full lg:w-6/12 px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="reward-contrat-balance"
                                    >
                                        Contract Total Reward Balance
                                    </label>
                                    <input
                                        disabled
                                        type="text"
                                        id="reward-contrat-balance"
                                        name="reward-contrat-balance"
                                        className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none w-full hover:none"
                                        value={0}
                                    />
                                </div>
                            </div>
                            <div className="w-full lg:w-6/12 px-4">
                                <div className="relative w-full mb-3">
                                    <label
                                        className="block uppercase text-slate-400 text-xs font-bold mb-2"
                                        htmlFor="reward-jeur"
                                    >
                                        My jEUR Reward
                                    </label>
                                    <input
                                        disabled
                                        type="text"
                                        id="reward-jeur"
                                        name="reward-jeur"
                                        className="border-0 px-3 py-3 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                        value={0}
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
                                        htmlFor="reward-withdrawal"
                                    >
                                        Withdrawal
                                    </label>
                                </div>

                                <div className="relative w-full mb-3 p-2 bg-white border shadow rounded">
                                    <div className="flex justify-between items-center">
                                        <input
                                            type="number"
                                            id="reward-withdrawal"
                                            name="reward-withdrawal"
                                            className={
                                                openValidationBuy
                                                    ? "hidden"
                                                    : "border-0 px-3 py-3 mr-2 h-10 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                            }
                                            // className="border-0 px-3 py-3 mr-2 h-10 placeholder-slate-300 text-slate-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                                            min="0"
                                            step="1"
                                            // onChange={false}
                                            readOnly
                                            value={inputValueBuy}
                                        />

                                        <div
                                            className={
                                                openValidationBuy
                                                    ? "ml-2"
                                                    : "hidden"
                                            }
                                        >
                                            {inputValueBuy}
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
                                            openValidationBuy
                                                ? " hidden"
                                                : " flex justify-between items-center"
                                        }
                                    >
                                        <button
                                            type="button"
                                            className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                                            onClick={() => {
                                                inputValueBuy > 0 &&
                                                    setOpenValidationBuy(
                                                        !openValidationBuy
                                                    );
                                            }}
                                        >
                                            Collect jEUR
                                        </button>
                                    </div>

                                    {/* <!-- container after clicked "EDIT" --> */}
                                    <div
                                        className={
                                            openValidationBuy
                                                ? " flex justify-between items-center"
                                                : " hidden"
                                        }
                                    >
                                        <div className="flex w-full justify-center items-center space-x-2">
                                            <div className="w-6/12">
                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        setOpenValidationBuy(
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
                                                    // onClick={false}
                                                    className="btn w-full bg-secondary hover:bg-secondary-dark text-white px-4 py-2 font-medium rounded"
                                                >
                                                    Confirm
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                    {/* <button
                    className="w-full bg-secondary text-white active:bg-sky-600 font-bold uppercase text-xs px-3 py-4 h-12 rounded shadow hover:shadow-md outline-none focus:outline-none ease-linear transition-all duration-150"
                    type="button"
                  >
                    Buy ECOR
                  </button> */}
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </>
    );
};
export default CardReward;
