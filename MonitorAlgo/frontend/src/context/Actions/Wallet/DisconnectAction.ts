import toast from "react-hot-toast";

const DisconnectAction = (dispatch: any) => {
    // localStorage.removeItem("token");
    dispatch({
        type: "account_disconnected",
    });
    dispatch({
        type: "network_disconnected",
    });
    window.localStorage.removeItem("isConnected");
    toast.success("Disconnected.");
};
export default DisconnectAction;
