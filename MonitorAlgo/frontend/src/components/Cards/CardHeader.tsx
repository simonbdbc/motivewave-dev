// import React from "react";
// import PropTypes from "prop-types";

// export default function CardHeader({
//   headerSubtitle,
//   headerTitle,
//   headerIconComponent,
//   headerIconColor,
// }) {
//   return (
//     <>
//       <div className="relative flex flex-col min-w-[207px] break-words bg-white rounded mb-6 lg:mb-0 shadow-lg">
//         <div className="flex-auto p-4">
//           <div className="flex flex-wrap">
//             <div className="relative w-full pr-4 max-w-full flex-grow flex-1">
//               <h5 className="text-slate-400 uppercase font-bold text-xs">
//                 {headerSubtitle}
//               </h5>
//               <span className="font-semibold text-xl text-slate-700">
//                 {headerTitle}
//               </span>
//             </div>
//             <div className="relative w-auto pl-4 flex-initial">
//               <div
//                 className={
//                   "text-white p-3 text-center inline-flex items-center justify-center w-12 h-12 shadow-lg rounded-full " +
//                   headerIconColor
//                 }
//               >
//                 {headerIconComponent}
//               </div>
//             </div>
//           </div>
//         </div>
//       </div>
//     </>
//   );
// }

// CardHeader.defaultProps = {
//   headerSubtitle: "",
//   headerTitle: "",
//   headerIconComponent: {},
//   headerIconColor: "",
// };

// // CardHeader.propTypes removed (TypeScript migration)
//   headerSubtitle: PropTypes.string,
//   headerTitle: PropTypes.string,
//   headerIconComponent: PropTypes.object,
//   // can be any of the background color utilities
//   // from tailwindcss
//   headerIconColor: PropTypes.string,
// };
import type { ReactNode } from "react";

export type CardHeaderProps = {
    headerSubtitle?: string;
    headerTitle?: string;
    headerIconComponent?: ReactNode;
    headerIconColor?: string;
};

const CardHeader = ({
    headerSubtitle = "",
    headerTitle = "",
    headerIconComponent = null,
    headerIconColor = "",
}: CardHeaderProps) => {
    return (
        <div className="relative flex flex-col min-w-[207px] break-words bg-white rounded mb-6 lg:mb-0 shadow-lg">
            <div className="flex-auto p-4">
                <div className="flex flex-wrap">
                    <div className="relative w-full pr-4 max-w-full flex-grow flex-1">
                        <h5 className="text-slate-400 uppercase font-bold text-xs">
                            {headerSubtitle}
                        </h5>
                        <span className="font-semibold text-xl text-slate-700">
                            {headerTitle}
                        </span>
                    </div>
                    <div className="relative w-auto pl-4 flex-initial">
                        <div
                            className={
                                "text-white p-3 text-center inline-flex items-center justify-center w-12 h-12 shadow-lg rounded-full " +
                                headerIconColor
                            }
                        >
                            {headerIconComponent}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CardHeader;
