// import React from "react";

const CardProfileInfo = () => {
  return (
    <>
      <div className="relative flex flex-col min-w-0 break-words bg-white w-full mb-6 shadow-xl rounded-lg">
        <div className="px-6">
          <div className="text-center mt-12">
            <h3 className="text-xl font-semibold leading-normal text-slate-700 mb-2">
              Jenna Stones
            </h3>
            <div className="text-sm leading-normal mt-0 mb-2 text-slate-400 font-bold uppercase">
              Los Angeles, California
            </div>
          </div>
          <div className="mt-10 py-10 border-t border-slate-200 text-center">
            <div className="flex flex-wrap justify-center">
              <div className="w-full lg:w-9/12 px-4">
                <p className="mb-4 text-lg leading-relaxed text-slate-700">
                  An artist of considerable range, Jenna the name taken by
                  Melbourne-raised, Brooklyn-based Nick Murphy writes, performs
                  and records all of his own music, giving it a warm, intimate
                  feel with a solid groove structure. An artist of considerable
                  range.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};
export default CardProfileInfo;
