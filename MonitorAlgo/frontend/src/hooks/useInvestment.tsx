// import React from "react";
// // import { ethers } from "ethers";
// import useLoaderContext from "context/LoaderContext";
// import useEvmGlobalContext from "context/EvmGlobalContext";
// // import useEvm from "./useEvm";
// import useHelper from "./useHelper";
// import toast from "react-hot-toast";

// const useInvestment = () => {
//     const { setLoader } = useLoaderContext();
//     const { getOverridesGas } = useHelper();
//     // const { loadBlockchainData } = useEvm();
//     const { walletState, investmentState, jeurState }: any =
//         useEvmGlobalContext();
//     const {
//         account: { allowanceAmountJEUR },
//     } = walletState;
//     // const {
//     //   investment: { contract: investmentContract, address: investmentAddress },
//     // } = investmentState;
//     // const {
//     //   jeur: { contract: jeurContract, address: jeurAddress },
//     // } = jeurState;

//     const toastId = { approve: null, buyTokens: null };

//     const txAllowance = async (amountToWei: any) => {
//         // const provider = new ethers.providers.Web3Provider(window.ethereum);
//         let txError = true;

//         setLoader(true);
//         // toastId.approve = toast.loading("Approve transaction in progress...");

//         try {
//             // let overrides = await getOverridesGas(provider, jeurContract, "approve", {
//             //   investmentAddress,
//             //   amountToWei,
//             // }
//             // );
//             // Allowance transaction
//             // const tx = await jeurContract.approve(
//             //   investmentAddress,
//             //   amountToWei,
//             //   overrides
//             // );
//             // // console.log("Approve transaction...");
//             // // console.log(`https://mumbai.polygonscan.com/tx/${tx.hash}`);
//             // setLoader(false);
//             // const reciept = await tx.wait();
//             // console.log(reciept.transactionHash);
//             // toast.success("Approval success", {
//             //   id: toastId.approve,
//             // });
//             // txError = false;
//             // toast(
//             //   <a
//             //     className="text-slate-700 hover:text-slate-500 text-xs uppercase py-3 font-bold block"
//             //     href={`https://mumbai.polygonscan.com/tx/${tx.hash}`}
//             //     target="blank"
//             //   >
//             //     click here to see tx on scan
//             //   </a>
//             // );
//             // catch
//         } catch (error) {
//             setLoader(false);
//             // toast.error("Approval cancel", {
//             //   id: toastId.approve,
//             // });
//             console.log(error);
//         }

//         return txError;
//     };

//     const txBuyTokens = async (amountToWei: any) => {
//         // const provider = new ethers.providers.Web3Provider(window.ethereum);

//         setLoader(true);
//         // toastId.buyTokens = toast.loading("Buy tokens transaction in progress...");

//         try {
//             // let overrides = await getOverridesGas(
//             //   provider,
//             //   investmentContract,
//             //   "buyTokens",
//             //   {
//             //     amountToWei,
//             //   }
//             // );
//             // BuyTokens transaction
//             // let tx = await investmentContract.buyTokens(amountToWei, overrides);
//             // // console.log("Mining transaction...");
//             // // console.log(`https://mumbai.polygonscan.com/tx/${tx.hash}`);
//             // setLoader(false);
//             // const reciept = await tx.wait();
//             // // console.log(reciept.transactionHash);
//             // toast.success("Token bought success", {
//             //   id: toastId.buyTokens,
//             // });
//             // toast(
//             //   <a
//             //     className="text-slate-700 hover:text-slate-500 text-xs uppercase py-3 font-bold block"
//             //     href={`https://mumbai.polygonscan.com/tx/${tx.hash}`}
//             //     target="blank"
//             //   >
//             //     click here to see tx on scan
//             //   </a>
//             // );
//             // catch
//         } catch (error) {
//             setLoader(false);
//             // toast.error("Token bought cancel", {
//             //   id: toastId.buyTokens,
//             // });
//             console.log(error);
//         }
//     };

//     const buyEcudorToken = async (inputValue: any) => {
//         if (!inputValue || inputValue === "0" || inputValue < 0) {
//             return false;
//         } else {
//             // let amountToWei = ethers.utils.parseUnits(inputValue, "ether");
//             // let allowanceAmountToWei = ethers.utils.parseUnits(
//             //   allowanceAmountJEUR,
//             //   "ether"
//             // );
//             // console.log(allowanceAmountToWei, amountToWei);
//             // console.log(allowanceAmountToWei < amountToWei);
//             // if (allowanceAmountToWei < amountToWei) {
//             //   // Allowance transaction
//             //   if (!await txAllowance(amountToWei.toString())) {
//             //     // BuyTokens transaction
//             //     await txBuyTokens(amountToWei.toString());
//             //   }
//             // } else {
//             //   // BuyTokens transaction
//             //   await txBuyTokens(amountToWei);
//             // }
//             // loadBlockchainData();
//         }
//     };

//     const setApprovalAmount = async (inputValue: any) => {
//         if (!inputValue || inputValue === "0" || inputValue < 0) {
//             return false;
//         } else {
//             // let amountToWei = ethers.utils.parseUnits(inputValue, "ether").toString();
//             // console.log(amountToWei);
//             // Allowance transaction
//             // await txAllowance(amountToWei);
//             // loadBlockchainData();
//         }
//     };

//     return {
//         buyEcudorToken,
//         setApprovalAmount,
//     };
// };

// export default useInvestment;
