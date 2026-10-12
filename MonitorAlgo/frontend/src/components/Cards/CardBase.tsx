// import React from "react";
// import PropTypes from "prop-types";

// export default function CardBase({
//   baseSubtitle,
//   baseTitle,
//   baseDescripiron,
//   baseIconName,
//   baseIconColor,
// }:any) {
//   return (
//     <>
//       <div className="relative flex flex-col min-w-[207px] break-words bg-white rounded mb-6 shadow-lg">
//         <div className="flex-auto p-4">
//           <div className="flex flex-wrap">
//             <div className="relative w-full pr-4 max-w-full flex-grow flex-1">
//               <h5 className="text-slate-400 uppercase font-bold text-xs">
//                 {baseSubtitle}
//               </h5>
//               <span className="font-semibold text-xl text-slate-700">
//                 {baseTitle}
//               </span>
//             </div>
//             {/* <div className="relative w-auto pl-4 flex-initial">
//               <div
//                 className={
//                   "text-white p-3 text-center inline-flex items-center justify-center w-12 h-12 shadow-lg rounded-full " +
//                   baseIconColor
//                 }
//               >
//                 <i className={baseIconName}></i>
//               </div>
//             </div> */}
//           </div>
//           <p className="text-sm text-slate-400 mt-4">
//             <span className="whitespace-nowrap">{baseDescripiron}</span>
//           </p>
//         </div>
//       </div>
//     </>
//   );
// }

// CardBase.defaultProps = {
//   baseSubtitle: "",
//   baseTitle: "",
//   baseDescripiron: "",
//   baseIconName: "",
//   baseIconColor: "",
// };

// // CardBase.propTypes removed (TypeScript migration)
//   baseSubtitle: PropTypes.string,
//   baseTitle: PropTypes.string,
//   // can be any of the text color utilities
//   // from tailwindcss
//   baseDescripiron: PropTypes.string,
//   baseIconName: PropTypes.string,
//   // can be any of the background color utilities
//   // from tailwindcss
//   baseIconColor: PropTypes.string,
// };
import React from "react";

// Définition des props, avec valeur par défaut possible
export type CardBaseProps = {
    baseSubtitle?: string;
    baseTitle?: string;
    baseDescripiron?: string;
    baseIconName?: string;
    baseIconColor?: string;
};

const CardBase: React.FC<CardBaseProps> = ({
    baseSubtitle = "",
    baseTitle = "",
    baseDescripiron = "",
    // baseIconName = "",
    // baseIconColor = "",
}) => {
    return (
        <div className="relative flex flex-col min-w-[207px] break-words bg-white rounded mb-6 shadow-lg">
            <div className="flex-auto p-4">
                <div className="flex flex-wrap">
                    <div className="relative w-full pr-4 max-w-full flex-grow flex-1">
                        <h5 className="text-slate-400 uppercase font-bold text-xs">
                            {baseSubtitle}
                        </h5>
                        <span className="font-semibold text-xl text-slate-700">
                            {baseTitle}
                        </span>
                    </div>
                    {/* 
          <div className="relative w-auto pl-4 flex-initial">
            <div
              className={
                "text-white p-3 text-center inline-flex items-center justify-center w-12 h-12 shadow-lg rounded-full " +
                baseIconColor
              }
            >
              <i className={baseIconName}></i>
            </div>
          </div>
          */}
                </div>
                <p className="text-sm text-slate-400 mt-4">
                    <span className="whitespace-nowrap">{baseDescripiron}</span>
                </p>
            </div>
        </div>
    );
};

export default CardBase;
