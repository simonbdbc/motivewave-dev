import { ArrowCircleRightIcon } from "icons";
import { BadgeCheckIcon } from "icons";
// import React from "react";

const CardInfo = () => {
  return (
    <>
      <div className="relative flex flex-col min-w-0 break-words bg-white w-full mb-6 shadow-xl rounded-lg">
        <div className="px-6">
          <div className="text-center mt-12">
            <h3 className="text-xl font-semibold leading-normal text-slate-700 mb-2">
              What's up ?
            </h3>
          </div>
          <div className="mt-10 pb-10 border-t border-slate-200 text-center">
            <div className="text-sm leading-normal mt-8 mb-2 text-slate-400 font-bold uppercase">
              Last news
            </div>
            <div className="flex flex-wrap justify-center text-start">
              <ul className="w-full">
                <li className="flex items-start mb-1 text-base leading-relaxed text-slate-700">
                  <BadgeCheckIcon className="inline mr-[4px] mt-[3px] w-[20px]" />
                  Ecudor polygon testnet is online !
                </li>
              </ul>
            </div>
            <div className="mt-8 text-sm leading-normal mb-2 text-slate-400 font-bold uppercase">
              Coming soon
            </div>
            <div className="flex flex-wrap justify-center text-start">
              <ul className="w-full">
                <li className="flex items-start mb-1 text-base leading-relaxed text-slate-700">
                  <ArrowCircleRightIcon className="inline mr-[4px] mt-[3px] w-[20px]" />
                  Ecudor polygon mainnet v1
                </li>
                <li className="flex items-start mb-1 text-base leading-relaxed text-slate-700">
                  <ArrowCircleRightIcon className="inline mr-[4px] mt-[3px] w-[20px]" />
                  Rewards
                </li>
                <li className="flex items-start mb-1 text-base leading-relaxed text-slate-700">
                  <ArrowCircleRightIcon className="inline mr-[4px] mt-[3px] w-[20px]" />
                  Stats
                </li>
                <li className="flex items-start mb-1 text-base leading-relaxed text-slate-700">
                  <ArrowCircleRightIcon className="inline mr-[4px] mt-[3px] w-[20px]" />
                  Gouvernance
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};
export default CardInfo;
