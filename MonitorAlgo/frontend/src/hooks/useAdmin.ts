// // import { ethers } from "ethers";
// import useEvmGlobalContext from "context/EvmGlobalContext";
// // import useEvm from "./useEvm";
// // import useHelper from "./useHelper";
// import useLoaderContext from "context/LoaderContext";
// // import toast from "react-hot-toast";

// const useAdmin = () => {
//     const { setLoader } = useLoaderContext();
//     // const { getOverridesGas } = useHelper();
//     // const { loadBlockchainData } = useEvm();
//     // const { walletState, investmentState, ecudorState, jeurState }: any =
//     //     useEvmGlobalContext();
//     // const {
//     //     account: { allowanceAmountJEUR },
//     // } = walletState;
//     // const {
//     //     ecudor: { contract: ecudorContract, address: ecudorAddress },
//     // } = ecudorState;
//     // const {
//     //   investment: { contract: investmentContract, address: investmentAddress },
//     // } = investmentState;
//     // const {
//     //   jeur: { contract: jeurContract, address: jeurAddress },
//     // } = jeurState;

//     // const toastId = { transferOwnership: null };

//     // const txOwnership = async (newAddress: any) => {
//         // const provider = new ethers.providers.Web3Provider(window.ethereum);

//         setLoader(true);
//         // toastId.transferOwnership = toast.loading(
//         //   "Transfer ownership in progress..."
//         // );

//         try {
//             // transferOwnership transaction
//             let tx = await ecudorContract.transferOwnership(newAddress);
//             setLoader(false);

//             const reciept = await tx.wait();
//             console.log("transactionHash", reciept.transactionHash);

//             // toast.success("Transfer success", {
//             //   id: toastId.transferOwnership,
//             // });

//             // catch
//         } catch (error) {
//             setLoader(false);

//             // if (error.code === -32603) {
//             //   toast.error("Caller is not the owner", {
//             //     id: toastId.transferOwnership,
//             //   });
//             // } else {
//             //   toast.error("Transfer error", {
//             //     id: toastId.transferOwnership,
//             //   });
//             // }
//             console.log(error);
//         }
//     };

//     const transferOwnership = async (inputValue: any) => {
//         //   if (!inputValue || inputValue === "") {
//         //     return false;
//         //   } else {
//         //     let newAddress = ethers.utils.getAddress(inputValue);
//         //     // console.log(newAddress);
//         //     // Ownership transaction
//         //     await txOwnership(newAddress);
//         //     loadBlockchainData();
//         //   }
//     };

//     return {
//         transferOwnership,
//     };
// };

// export default useAdmin;
