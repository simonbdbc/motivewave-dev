// import React from "react";
// import Chart from "chart.js/auto";

// export default function CardLineChart() {
//   React.useEffect(() => {
//     if (window.myLine) {
//       window.myLine.destroy();
//     }
//     var config = {
//       type: "line",
//       data: {
//         labels: [
//           "Jan",
//           "Feb",
//           "Mar",
//           "Apr",
//           "May",
//           "Jun",
//           "Jul",
//           "Aug",
//           "Sep",
//           "Oct",
//           "Nov",
//           "Dec",
//         ],
//         datasets: [
//           {
//             label: new Date().getFullYear(),
//             backgroundColor: "#4c51bf",
//             borderColor: "#4c51bf",
//             data: [65, 78, 66, 44, 56, 67, 75, 60, 70, 69, 48, 50],
//             fill: false,
//           },
//           {
//             label: new Date().getFullYear() - 1,
//             fill: false,
//             backgroundColor: "#fff",
//             borderColor: "#fff",
//             data: [40, 68, 86, 74, 56, 60, 87, 48, 60, 86, 79, 68],
//           },
//         ],
//       },
//       options: {
//         maintainAspectRatio: false,
//         responsive: true,
//         title: {
//           display: false,
//           text: "Sales Charts",
//           fontColor: "white",
//         },
//         legend: {
//           labels: {
//             fontColor: "white",
//           },
//           align: "end",
//           position: "bottom",
//         },
//         tooltips: {
//           mode: "index",
//           intersect: false,
//         },
//         hover: {
//           mode: "nearest",
//           intersect: true,
//         },
//         scales: {
//           x: {
//             ticks: {
//               fontColor: "rgba(255,255,255,.7)",
//             },
//             display: true,
//             scaleLabel: {
//               display: false,
//               labelString: "Month",
//               fontColor: "white",
//             },
//             gridLines: {
//               display: false,
//               borderDash: [2],
//               borderDashOffset: [2],
//               color: "rgba(33, 37, 41, 0.3)",
//               zeroLineColor: "rgba(0, 0, 0, 0)",
//               zeroLineBorderDash: [2],
//               zeroLineBorderDashOffset: [2],
//             },
//           },
//           y: {
//             ticks: {
//               fontColor: "rgba(255,255,255,.7)",
//             },
//             display: true,
//             scaleLabel: {
//               display: false,
//               labelString: "Value",
//               fontColor: "white",
//             },
//             gridLines: {
//               borderDash: [3],
//               borderDashOffset: [3],
//               drawBorder: false,
//               color: "rgba(255, 255, 255, 0.15)",
//               zeroLineColor: "rgba(33, 37, 41, 0)",
//               zeroLineBorderDash: [2],
//               zeroLineBorderDashOffset: [2],
//             },
//           },
//         },
//       },
//     };
//     var ctx = document.getElementById("line-chart").getContext("2d");
//     window.myLine = new Chart(ctx, config);
//   }, []);
//   return (
//     <>
//       <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded bg-slate-700">
//         <div className="rounded-t mb-0 px-4 py-3 bg-transparent">
//           <div className="flex flex-wrap items-center">
//             <div className="relative w-full max-w-full flex-grow flex-1">
//               <h6 className="uppercase text-slate-100 mb-1 text-xs font-semibold">
//                 Performance
//               </h6>
//               <h2 className="text-white text-xl font-semibold">Rewards</h2>
//             </div>
//           </div>
//         </div>
//         <div className="p-4 flex-auto">
//           {/* Chart */}
//           <div className="relative h-350-px">
//             <canvas id="line-chart"></canvas>
//           </div>
//         </div>
//       </div>
//     </>
//   );
// }
import React, { useRef, useEffect } from "react";
import Chart from "chart.js/auto";

const currentYear = new Date().getFullYear();

const CardLineChart: React.FC = () => {
    const chartRef = useRef<HTMLCanvasElement | null>(null);
    const chartInstance = useRef<Chart | null>(null);

    useEffect(() => {
        if (chartInstance.current) {
            chartInstance.current.destroy();
        }
        if (!chartRef.current) return;

        // Config Chart.js v3+ (options/plugins adaptés)
        const config: any = {
            type: "line",
            data: {
                labels: [
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec",
                ],
                datasets: [
                    {
                        label: (currentYear - 1).toString(),
                        backgroundColor: "#4c51bf",
                        borderColor: "#4c51bf",
                        data: [1500, 4500, 3000, 0, 1500, 3000, 4500, 3000, 4500, 1500, 0, 1500],
                        fill: false,
                    },
                    {
                        label: currentYear.toString(),
                        fill: false,
                        backgroundColor: "#fff",
                        borderColor: "#fff",
                        data: [0, 1500, 4500, 3000, 1500, 1500, 4500, 0, 1500, 0, 1500, 3000],
                    },
                ],
            },
            options: {
                maintainAspectRatio: false,
                responsive: true,
                plugins: {
                    legend: {
                        labels: {
                            color: "white",
                        },
                        align: "end",
                        position: "bottom",
                    },
                    title: {
                        display: false,
                        text: "Sales Charts",
                        color: "white",
                    },
                    tooltip: {
                        mode: "index" as const,
                        intersect: false,
                    },
                },
                hover: {
                    mode: "nearest",
                    intersect: true,
                },
                scales: {
                    x: {
                        ticks: {
                            color: "rgba(255,255,255,.7)",
                        },
                        display: true,
                        title: {
                            display: false,
                            text: "Month",
                            color: "white",
                        },
                        grid: {
                            display: false,
                            borderDash: [2],
                            color: "rgba(33, 37, 41, 0.3)",
                            zeroLineColor: "rgba(0, 0, 0, 0)", // deprecated in v3
                            zeroLineBorderDash: [2], // deprecated in v3
                        },
                    },
                    y: {
                        ticks: {
                            color: "rgba(255,255,255,.7)",
                        },
                        display: true,
                        title: {
                            display: false,
                            text: "Value",
                            color: "white",
                        },
                        grid: {
                            borderDash: [3],
                            drawBorder: false,
                            color: "rgba(255, 255, 255, 0.15)",
                            zeroLineColor: "rgba(33, 37, 41, 0)", // deprecated in v3
                            zeroLineBorderDash: [2], // deprecated in v3
                        },
                    },
                },
            },
        };

        chartInstance.current = new Chart(chartRef.current, config);

        return () => {
            if (chartInstance.current) {
                chartInstance.current.destroy();
            }
        };
    }, []);

    return (
        <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded bg-slate-700">
            <div className="rounded-t mb-0 px-4 py-3 bg-transparent">
                <div className="flex flex-wrap items-center">
                    <div className="relative w-full max-w-full flex-grow flex-1">
                        <h6 className="uppercase text-slate-100 mb-1 text-xs font-semibold">
                            Performance
                        </h6>
                        <h2 className="text-white text-xl font-semibold">
                            Payouts
                        </h2>
                    </div>
                </div>
            </div>
            <div className="p-4 flex-auto">
                <div className="relative h-350-px">
                    <canvas ref={chartRef} />
                </div>
            </div>
        </div>
    );
};

export default CardLineChart;
