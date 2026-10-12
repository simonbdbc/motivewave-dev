// // import { ethers } from "ethers";
// import useLoaderContext from "context/LoaderContext";
// import useEvmGlobalContext from "context/EvmGlobalContext";
// // import useEvm from "./useEvm";
// import useHelper from "./useHelper";
// // import toast from "react-hot-toast";

// const useTesting = () => {
//     const { setLoader } = useLoaderContext();
//     const { getOverridesGas } = useHelper();
//     // const { loadBlockchainData } = useEvm();
//     const { walletState, jeurState }: any = useEvmGlobalContext();
//     const {
//         account: { address },
//     } = walletState;
//     const {
//         jeur: { contract: jeurContract },
//     } = jeurState;

//     const toastId = { claimJeur: null };

//     const claimMockJEUR = async () => {
//         // const provider = new ethers.providers.Web3Provider(window.ethereum);

//         setLoader(true);
//         // toastId.claimJeur = toast.loading("Claim jEUR transaction in progress...");

//         try {
//             // let amountToWei = ethers.utils.parseUnits("100", "ether").toString();
//             // let overrides = await getOverridesGas(
//             //   provider,
//             //   jeurContract,
//             //   "claimJeur",
//             //   {
//             //     address,
//             //     amountToWei,
//             //   }
//             // );
//             // ClaimJeur transaction
//             // let tx = await jeurContract.claimJeur(address, amountToWei, overrides);
//             // // console.log("Mining transaction...");
//             // // console.log(`https://mumbai.polygonscan.com/tx/${tx.hash}`);
//             // setLoader(false);
//             // const reciept = await tx.wait();
//             // // console.log(reciept.transactionHash);
//             // toast.success("Claim jEUR success", {
//             //   id: toastId.claimJeur,
//             // });
//             // loadBlockchainData();
//             // catch
//         } catch (error) {
//             setLoader(false);
//             // toast.error("Claim jEUR cancel", {
//             //   id: toastId.claimJeur,
//             // });
//             console.log(error);
//         }
//     };

//     return {
//         claimMockJEUR,
//     };
// };

// export default useTesting;
