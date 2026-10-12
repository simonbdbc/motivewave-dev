import toast from "react-hot-toast";

const SetAccountAction = (dispatch: any, data: any) => {
    if (data.length === 0) {
        // MetaMask is locked or the user has not connected any accounts
        dispatch({
            type: "account_error",
            payload: "Error change account.",
        });
        toast.error("Account error.");
    } else {
        dispatch({
            type: "account_field",
            field: "address",
            payload: data,
        });
        dispatch({
            type: "account_connected",
        });
        window.localStorage.setItem("isConnected", "true");
    }
};

export default SetAccountAction;
