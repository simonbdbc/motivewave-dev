// import { useCallback } from "react";
// import { ethers } from "ethers";
// import toast from "react-hot-toast";

// import useEvmGlobalContext from "context/EvmGlobalContext";
// import chainData from "data/chains.json";

// import SetAccountAction from "context/Actions/Wallet/SetAccountAction";
// import DisconnectAction from "context/Actions/Wallet/DisconnectAction";
// import EcudorToken from "abis/polygon/EcudorToken.json";
// import JeurToken from "abis/polygon/JeurToken.json";
// import EcudorInvestment from "abis/polygon/EcudorInvestment.json";
// import SetNetworkAction from "context/Actions/Wallet/SetNetworkAction";
// import SetUserEcudorBalanceAction from "context/Actions/Wallet/SetUserEcudorBalanceAction";
// import SetUserJeurBalanceAction from "context/Actions/Wallet/SetUserJeurBalanceAction";
// import SetInvestmentContractBalanceAction from "context/Actions/Investment/SetInvestmentContractBalanceAction";
// import SetInvestmentContractAddressAction from "context/Actions/Investment/SetInvestmentContractAddressAction";
// import SetInvestmentContractAction from "context/Actions/Investment/SetInvestmentContractAction";
// import SetEcudorContractAction from "context/Actions/Ecudor/SetEcudorContractAction";
// import SetEcudorContractAddressAction from "context/Actions/Ecudor/SetEcudorContractAddressAction";
// import SetEcudorContractOwnerAction from "context/Actions/Ecudor/SetEcudorContractOwnerAction";
// import SetEcudorContractBalanceAction from "context/Actions/Ecudor/SetEcudorContractBalanceAction";
// import SetJeurContractAction from "context/Actions/Jeur/SetJeurContractAction";
// import SetJeurContractAddressAction from "context/Actions/Jeur/SetJeurContractAddressAction";
// import SetUserJeurAllowanceAmountAction from "context/Actions/Wallet/SetUserJeurAllowanceAmountAction";

// const useEvm = () => {
//   const {
//     walletDispatch,
//     walletState,
//     ecudorDispatch,
//     jeurDispatch,
//     investmentDispatch,
//     rewardDispatch,
//   } :any= useEvmGlobalContext();
//   const allDispatch = {
//     walletDispatch,
//     ecudorDispatch,
//     jeurDispatch,
//     investmentDispatch,
//     rewardDispatch,
//   };
//   const {
//     account: { address: userAddress, isConnected },
//     // network: { id: networkId },
//   } = walletState;

//   const { ethereum }:any = window;
//   const networks = chainData;

//   const getCurrentNetwork = useCallback(
//     async (signer:any) => {
//       if (!signer) return;

//       try {
//         let networkId = await signer.getChainId();
//         SetNetworkAction(walletDispatch, networkId);
//         // console.log("network--------------------------");
//         // console.log(networkId);
//         return networkId;
//       } catch (error) {
//         // toast.error("Error on detected network.", {
//         //   toastId: "errorNetwork",
//         // });
//         DisconnectAction(walletDispatch);
//         throw error;
//       }
//     },
//     [walletDispatch]
//   );

//   const getCurrentWallet = useCallback(
//     async (signer:any) => {
//       if (!signer) return;

//       let address = await signer.getAddress();
//       SetAccountAction(walletDispatch, address);
//       // console.log("address--------------------------");
//       // console.log(address);
//     },
//     [walletDispatch]
//   );

//   const getContractEcudorToken = useCallback(
//     async (signer:any) => {
//       if (!signer) return;

//       try {
//         let networkId = await signer.getChainId();
//         // const ecudorTokenData = EcudorToken.networks[networkId];
//         // const ecudorTokenContract = new ethers.Contract(
//         //   ecudorTokenData.address,
//         //   EcudorToken.abi,
//         //   signer
//         // );
//         // console.log("ecudorTokenContract--------------------------");
//         // console.log(ecudorTokenContract);
//         // SetEcudorContractAction(ecudorDispatch, ecudorTokenContract);
//         // // console.log("ecudorTokenContractAddress--------------------------");
//         // // console.log(ecudorTokenData.address);
//         // SetEcudorContractAddressAction(ecudorDispatch, ecudorTokenData.address);

//         // let ecudorContractOwner = await ecudorTokenContract.owner();
//         // SetEcudorContractOwnerAction(
//         //   ecudorDispatch,
//         //   ecudorContractOwner
//         // );

//         // let ecudorContractBalance = await ecudorTokenContract.totalSupply();
//         // let ecudorContractBalanceConverted = parseInt(
//         //   ethers.utils.formatUnits(ecudorContractBalance, "ether")
//         // ).toFixed(0);
//         // // console.log("ecudorContractBalanceConverted--------------------------");
//         // // console.log(ecudorContractBalanceConverted);
//         // SetEcudorContractBalanceAction(
//         //   ecudorDispatch,
//         //   ecudorContractBalanceConverted
//         // );

//         // return ecudorTokenContract;
//       } catch (error) {
//         // toast.error("Ecudor Token contract not deployed to detected network.", {
//         //   toastId: "ecudorContract",
//         // });
//         // DisconnectAction(walletDispatch);
//         throw error;
//       }
//     },
//     [ecudorDispatch, walletDispatch]
//   );

//   const getUserBalanceEcudor = useCallback(
//     async (signer:any, ecudorTokenContract:any) => {
//       if (!signer || !ecudorTokenContract) return;

//       let userEcudorTokenBalance = await ecudorTokenContract.balanceOf(
//         signer.getAddress()
//       );
//       let userEcudorTokenBalanceConverted = parseInt(
//         ethers.utils.formatUnits(userEcudorTokenBalance, "ether")
//       ).toFixed(0);
//       // console.log("userEcudorTokenBalanceConverted--------------------------");
//       // console.log(userEcudorTokenBalanceConverted);
//       SetUserEcudorBalanceAction(
//         walletDispatch,
//         userEcudorTokenBalanceConverted
//       );
//     },
//     [walletDispatch]
//   );

//   const getContractInvestment = useCallback(
//     async (signer, ecudorTokenContract) => {
//       if (!signer || !ecudorTokenContract) return;

//       try {
//         let networkId = await signer.getChainId();
//         const ecudorInvestmentData = EcudorInvestment.networks[networkId];
//         const ecudorInvestmentContract = new ethers.Contract(
//           ecudorInvestmentData.address,
//           EcudorInvestment.abi,
//           signer
//         );
//         // console.log("ecudorInvestmentContract--------------------------");
//         // console.log(ecudorInvestmentContract);
//         SetInvestmentContractAction(
//           investmentDispatch,
//           ecudorInvestmentContract
//         );
//         // console.log(
//         //   "ecudorInvestmentContractAddress--------------------------"
//         // );
//         // console.log(ecudorInvestmentData.address);
//         SetInvestmentContractAddressAction(
//           investmentDispatch,
//           ecudorInvestmentData.address
//         );

//         let investmentContractBalance = await ecudorTokenContract.balanceOf(
//           ecudorInvestmentData.address
//         );
//         let investmentContractBalanceConverted = parseInt(
//           ethers.utils.formatUnits(investmentContractBalance, "ether")
//         ).toFixed(0);
//         // console.log(
//         //   "investmentContractBalanceConverted--------------------------"
//         // );
//         // console.log(investmentContractBalanceConverted);
//         SetInvestmentContractBalanceAction(
//           investmentDispatch,
//           investmentContractBalanceConverted
//         );

//         return ecudorInvestmentContract;
//       } catch (error) {
//         toast.error("Investment contract not deployed to detected network.", {
//           toastId: "investmentContract",
//         });
//         DisconnectAction(walletDispatch);
//         throw error;
//       }
//     },
//     [investmentDispatch, walletDispatch]
//   );

//   const getContractJeur = useCallback(
//     async (signer) => {
//       if (!signer) return;

//       try {
//         let networkId = await signer.getChainId();
//         const jeurTokenData = JeurToken.networks[networkId];
//         const jeurTokenContract = new ethers.Contract(
//           jeurTokenData.address,
//           JeurToken.abi,
//           signer
//         );
//         // console.log("jeurTokenContract--------------------------");
//         // console.log(jeurTokenContract);
//         SetJeurContractAction(jeurDispatch, jeurTokenContract);
//         // console.log("jeurTokenContractAddress--------------------------");
//         // console.log(jeurTokenData.address);
//         SetJeurContractAddressAction(jeurDispatch, jeurTokenData.address);

//         return jeurTokenContract;
//       } catch (error) {
//         throw error;
//       }
//     },
//     [jeurDispatch]
//   );

//   const getUserBalanceJeur = useCallback(
//     async (signer, jeurTokenContract) => {
//       if (!signer || !jeurTokenContract) return;

//       let userJeurTokenBalance = await jeurTokenContract.balanceOf(
//         signer.getAddress()
//       );
//       let userJeurTokenBalanceConverted = parseInt(
//         ethers.utils.formatUnits(userJeurTokenBalance, "ether")
//       ).toFixed(2);
//       // console.log("userJeurTokenBalanceConverted--------------------------");
//       // console.log(userJeurTokenBalanceConverted);
//       SetUserJeurBalanceAction(walletDispatch, userJeurTokenBalanceConverted);
//     },
//     [walletDispatch]
//   );

//   const getUserJeurAllowanceAmount = useCallback(
//     async (signer, jeurTokenContract) => {
//       if (!signer || !jeurTokenContract) return;

//       let networkId = await signer.getChainId();
//       const ecudorInvestmentData = EcudorInvestment.networks[networkId];

//       let userJeurAllowanceAmount = await jeurTokenContract.allowance(
//         signer.getAddress(),
//         ecudorInvestmentData.address
//       );

//       let userJeurAllowanceAmountConverted = parseInt(
//         ethers.utils.formatUnits(userJeurAllowanceAmount, "ether")
//       ).toFixed(0);
//       // console.log("userJeurAllowanceAmountConverted--------------------------");
//       // console.log(userJeurAllowanceAmountConverted);
//       SetUserJeurAllowanceAmountAction(
//         walletDispatch,
//         userJeurAllowanceAmountConverted
//       );
//     },
//     [walletDispatch]
//   );

//   const loadBlockchainData = useCallback(async () => {
//     //initializing function for our context
//     try {
//       const provider = new ethers.providers.Web3Provider(ethereum);
//       const signer = provider.getSigner();

//       await getCurrentNetwork(signer);
//       await getCurrentWallet(signer);

//       const ecudorTokenContract = await getContractEcudorToken(signer);
//       await getUserBalanceEcudor(signer, ecudorTokenContract);

//       await getContractInvestment(signer, ecudorTokenContract);

//       const jeurTokenContract = await getContractJeur(signer);
//       await getUserBalanceJeur(signer, jeurTokenContract);
//       await getUserJeurAllowanceAmount(signer, jeurTokenContract);
//     } catch (error) {
//       throw error;
//     }
//   }, [
//     getCurrentNetwork,
//     getCurrentWallet,
//     getContractEcudorToken,
//     getUserBalanceEcudor,
//     getContractInvestment,
//     getContractJeur,
//     getUserBalanceJeur,
//     getUserJeurAllowanceAmount,
//   ]);

//   const selectNetworkId = useCallback(
//     (networkId) => {
//       for (const idChain in chainData) {
//         if (Object.hasOwnProperty.call(chainData, idChain)) {
//           const selectedChain = chainData[idChain];
//           // console.log("chain", selectedChain);
//           // console.log("idChain", idChain);
//           // console.log("chainData", chainData);
//           // console.log("networkId", networkId);
//           if (networkId === selectedChain.chainId) {
//             return idChain;
//           }
//         }
//       }
//       return false;
//     },
//     [chainData]
//   );

//   const handleNetwork = async (idChain) => {
//     try {
//       // console.log("chainId",idChain);
//       await ethereum.request({
//         method: "wallet_switchEthereumChain",
//         params: [{ chainId: ethers.utils.hexValue(networks[idChain].chainId) }],
//       });
//     } catch (error) {
//       // console.log(error);
//       // This error code indicates that the chain has not been added to MetaMask
//       if (error.code === 4902) {
//         networks[idChain].chainId = ethers.utils.hexValue(
//           networks[idChain].chainId
//         );
//         await ethereum.request({
//           method: "wallet_addEthereumChain",
//           params: [
//             {
//               ...networks[idChain],
//             },
//           ],
//         });
//       } else {
//         console.error(error);
//         toast.error(error.message);
//       }
//     }
//   };

//   const handleAccountsChanged = useCallback(
//     (accounts) => {
//       if (accounts.length > 0) {
//         loadBlockchainData();
//       } else {
//         DisconnectAction(walletDispatch);
//       }
//     },
//     [walletDispatch]
//   );

//   const loadWeb3 = async () => {
//     if (typeof ethereum === "undefined") {
//       toast.error("MetaMask is not installed.");
//       return false;
//     }

//     // const chainId = 5777; // Ganache
//     // const chainId = 80001; // Mumbai
//     const idChain = selectNetworkId(80001);

//     if (idChain == false) {
//       toast.error("Error on detected network.");
//       return false;
//     }

//     if (ethereum.on) {
//       // if (ethereum.removeListener) {
//       //   ethereum.removeListener("chainChanged", () => loadBlockchainData());
//       //   ethereum.removeListener("accountsChanged", handleAccountsChanged);
//       // }
//       ethereum.on("chainChanged", () => loadBlockchainData());
//       ethereum.on("accountsChanged", handleAccountsChanged);
//     }

//     // console.log(window.ethereum.networkVersion, idChain);
//     if (
//       window.ethereum.networkVersion !== idChain.toString() &&
//       window.ethereum.networkVersion !== "1337"
//     ) {
//       await handleNetwork(idChain);
//     } else {
//       ethereum
//         .request({ method: "eth_requestAccounts" })
//         .then(loadBlockchainData)
//         .then(() => {
//           toast.success("Account connected.");
//         })
//         .catch((error) => {
//           console.log(error);
//           return;
//         });
//     }
//   };

//   const connectWallet = async () => {
//     await loadWeb3();
//   };
//   const autoConnectWallet = async () => {
//     if (window.localStorage.getItem("isConnected") && !isConnected) {
//       await loadWeb3();
//     }
//   };

//   return {
//     autoConnectWallet,
//     connectWallet,
//     loadBlockchainData,
//   };
// };

// export default useEvm;
