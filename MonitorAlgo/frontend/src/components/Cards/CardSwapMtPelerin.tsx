// import React from "react";

const CardSwapMtPelerin = () => {
  return (
    <>
      <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-white border-0">
        <div className="rounded-t bg-white mb-0 px-6 py-6">
          <div className="text-center flex justify-between">
            <h6 className="text-slate-700 text-xl font-bold">
              Buy & sell jEUR
            </h6>
          </div>
        </div>
        <div className="flex-auto px-4 lg:px-10 py-10 pt-0">
          {/* <div className="flex flex-wrap mt-10 mb-6"> */}
          <div className="flex flex-wrap mb-6">
            <div className="w-full lg:w-12/12 px-4">
              <div className="relative w-full mb-3">
                <label
                  className="block uppercase text-slate-400 text-xs font-bold mb-2"
                  htmlFor="grid-password"
                >
                  Be careful, disclaimer, swap is on mainnet, use it only for
                  real live swap
                </label>
              </div>
            </div>
            <div className="w-full lg:w-12/12 px-4">
              <div className="relative w-full mb-3">
                <div className=" flex justify-between items-center">
                  <iframe
                    src="https://widget.mtpelerin.com/?type=web&lang=en&tab=buy&bsc=EUR&bdc=jEUR&net=matic_mainnet&primary=%2377bde1&success=%2377bde1"
                    frameBorder="no"
                    className="w-full h-[600px]"
                  ></iframe>
                  {/* <iframe src="https://widget.mtpelerin.com/?lang=en"></iframe> */}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};
export default CardSwapMtPelerin;
