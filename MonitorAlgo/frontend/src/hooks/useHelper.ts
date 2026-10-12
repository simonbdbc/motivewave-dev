const useHelper = () => {
    const minifyDisplayAccount = (address: any) => {
        if (address.slice(0, 2) === "0x") {
            let beginning = address.slice(0, 6);
            let end = address.slice(address.length - 4, address.length);
            return beginning + "..." + end;
        } else {
            return address;
        }
    };

    const formatNumberDevise = (number: any, devise: any) => {
        return (
            (number ? number.replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,") : "0") +
            " " +
            devise
        );
    };

    const getOverridesGas = async (
        provider: any,
        contract: any,
        contractMethod: any,
        args: any
    ) => {
        // console.log(provider, contract, contractMethod, ...Object.values(args));
        // let overrides = {
        //   gasLimit: "1000000",
        //   gasPrice: "20000000000",
        //   maxFeePerGas: feeData.maxFeePerGas.toString(),
        //   maxPriorityFeePerGas: feeData.maxPriorityFeePerGas.toString(),
        // };
        let overrides = {
            gasLimit: "1000000",
            gasPrice: "60000000000",
        };

        try {
            let gasEstimated;
            gasEstimated = await contract.estimateGas[contractMethod](
                ...Object.values(args),
                overrides
            );
            overrides.gasLimit = gasEstimated.toString();
        } catch (error) {
            console.error("error estimateGas");
            console.error(error);
            return false;
        }

        try {
            let feeData = await provider.getFeeData();
            overrides.gasPrice = feeData.gasPrice.toString();
        } catch (error) {
            console.error("error getFeeData");
            console.error(error);
            return false;
        }
        // console.log("overridesGas--------------------");
        // console.log(overrides);
        return overrides;
    };

    return {
        minifyDisplayAccount,
        formatNumberDevise,
        getOverridesGas,
    };
};

export default useHelper;
