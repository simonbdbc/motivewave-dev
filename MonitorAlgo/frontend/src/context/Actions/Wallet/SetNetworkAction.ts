// Si tu as le droit d'écrire dans le fichier chains.json, fais-le typé comme ça :
type ChainDataType = {
    [key: string]: {
        chainId: number;
        chainName: string;
        nativeCurrency: {
            name: string;
            symbol: string;
            decimals: number;
        };
        rpcUrls: string[];
        blockExplorerUrls?: string[];
        [other: string]: any;
    };
};

import chainDataRaw from "data/chains.json";
const chainData: ChainDataType = chainDataRaw;

type SetNetworkActionFn = (dispatch: any, data: string | number) => void;

const SetNetworkAction: SetNetworkActionFn = (dispatch, data) => {
    const convertSelectedNetwork = () => {
        for (const id in chainData) {
            if (Object.prototype.hasOwnProperty.call(chainData, id)) {
                const chain = chainData[id];
                if (data.toString() === chain.chainId.toString()) {
                    return {
                        error: false,
                        network: { id: chain.chainId, name: id },
                    };
                }
            }
        }
        return { error: true, network: {} };
    };

    const { error, network } = convertSelectedNetwork();

    if (!error) {
        dispatch({
            type: "network_field",
            field: "id",
            payload: network.id,
        });
        dispatch({
            type: "network_field",
            field: "name",
            payload: network.name,
        });
        dispatch({
            type: "network_connected",
        });
    } else {
        dispatch({
            type: "network_error",
            payload: "Network error.",
        });
    }
};

export default SetNetworkAction;
