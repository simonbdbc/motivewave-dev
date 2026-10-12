const SetUsdcContractAction = (dispatch:any, data:any) => {
  dispatch({
    type: "usdc_field",
    field: "contract",
    payload: data,
  });
  dispatch({
    type: "usdc_loaded",
  });
};

export default SetUsdcContractAction;
