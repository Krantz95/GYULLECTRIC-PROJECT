// 불량예측 전용  //

// [더미용] 데이터 생성 함수 (불량예측 페이지와 동일 구조)
function generateDummyPredictionData() {
    const castingScore = Math.floor(Math.random() * 40) + 60;     // 60~99
    const weldingPercent = Math.floor(Math.random() * 40) + 50;   // 50~89
    return {
        castingScore,
        weldingPercent,
        castingWarning: castingScore >= 80 ? "공정에서 이상치 발견 (압력/온도)"
            : castingScore >= 61 ? "압력 기준치 접근 중 (주의)" : "안정",
        weldingWarning: weldingPercent >= 80 ? "설비에서 이상치 발견 (출력 급감)"
            : weldingPercent >= 61 ? "용접 출력 기준치 접근 중 (주의)" : "안정"
    };
}

// [더미용] 차트 대상 및 상태 div 정보
const datasets = [
    {
        id: 'castingChart',
        statusId: 'castingStatus',
        warningId: 'castingWarning',
        label: '압력/온도 감지',
        msgKey: 'casting'
    },
    {
        id: 'weldingChart',
        statusId: 'weldingStatus',
        warningId: 'weldingWarning',
        label: '용접 출력 감지',
        msgKey: 'welding'
    }
];

// [더미용] 페이지 로딩 시 5초마다 더미 데이터로 갱신
document.addEventListener("DOMContentLoaded", function () {
    setInterval(() => {
        const dummyData = generateDummyPredictionData();
        updateRealtimeCharts(dummyData);
        updateRealtimeWarnings(dummyData);
        updateStatusBoxes(dummyData);
    }, 5000);
});

// 📌 경고 메시지 박스: 상단 + 증상 항목별
const warningDatasets = [
    { warningId: "pressure-warning", msgKey: "pressure" },
    { warningId: "upperTemp-warning", msgKey: "upperTemp" },
    { warningId: "lowerTemp-warning", msgKey: "lowerTemp" },
    { warningId: "welding-warning", msgKey: "welding" }
];

// [ 1. 기본차트 생성해놓기 ]
datasets.forEach(data => {
    const canvas = document.getElementById(data.id);
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.parentNode.offsetWidth;

    const bgColor = data.value <= 60 ? 'green' : (data.value <= 79 ? '#FFD700' : 'red');

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [data.value, 100 - data.value],
                backgroundColor: [bgColor, '#878787'],
                borderWidth: 0
            }]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            circumference: 180,
            rotation: 270,
            cutout: '50%',
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false }
            }
        },
        plugins: [{
            afterDraw: chart => { // 도넛 중앙에 수치 표시
                const { ctx, chartArea: { width, height } } = chart;
                const centerX = width / 2;
                const centerY = (chart.chartArea.top + chart.chartArea.bottom) / 2;

                // ✅ 여기서 percent 안전하게 추출
                const dataset = chart.data.datasets[0];
                const rawValue = dataset?.data?.[0];
                const percent = Number.isFinite(rawValue) ? Math.round(rawValue) : 0;

                ctx.save();
                ctx.beginPath();
                ctx.arc(centerX, centerY, 50, 0, 2 * Math.PI);
                ctx.fillStyle = '#ffffff';
                ctx.fill();
                ctx.strokeStyle = '#F37221';
                ctx.stroke();
                ctx.font = '25px sans-serif';
                ctx.fillStyle = 'black';
                ctx.textAlign = 'center';
                ctx.fillText(`${percent}%`, centerX, centerY +10); // 퍼센트 안 글씨조금밑으로
                ctx.restore();
            }
        }]
    });

    // 상태 표시
    const statusDiv = document.getElementById(data.statusId);
    const warningDiv = document.getElementById(data.warningId);
    const isWarning = data.value >= 80;
    statusDiv.querySelector('.defect-btn').disabled = !isWarning;
    statusDiv.querySelector('.defect-btn').onclick = isWarning ? () => {
        window.location.href = '/indicators/defect-predict';
    } : null;
    statusDiv.querySelector('.defect-label').textContent = `${data.label} (${data.value}%)`;

    if (isWarning) {
        warningDiv.className = 'warning-box blinking warning-placeholder';
        warningDiv.innerHTML = `⚠ 주의! ${data.label} 초과 경고`;
    } else {
        warningDiv.className = 'warning-placeholder';
        warningDiv.innerHTML = '';
    }
});

// [ 2. 차트업데이트 - 실시간 차트 생성 ]
function updateRealtimeCharts(data) {
    datasets.forEach(ds => {
        // 로딩 메시지 숨기고 캔버스 보이게
        const loadingDiv = document.getElementById(ds.id + 'Loading');
        const canvas = document.getElementById(ds.id);
        if (loadingDiv) loadingDiv.style.display = 'none';
        if (canvas) canvas.style.display = 'block';

        const ctx = canvas.getContext('2d');
        const value = data[`${ds.msgKey}Score`] ?? data[`${ds.msgKey}Percent`] ?? 0;
        const percent = Math.round(value);
        const bgColor = percent >= 80 ? '#F44336' : percent >= 61 ? '#FFD700' : '#4CAF50';

        // 기존 차트 제거
        const existingChart = Chart.getChart(canvas);
        if (existingChart) existingChart.destroy();

        // 새 차트 생성 + 해당 캔버스에 저장
        canvas.chartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [percent, 100 - percent],
                    backgroundColor: [bgColor, '#878787'],
                    borderWidth: 0
                }]
            },
            options: {
                maintainAspectRatio: false,
                responsive: true,
                circumference: 180,
                rotation: 270,
                cutout: '50%',
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            },
            plugins: [{
                afterDraw: chart => {
                    const { ctx, chartArea: { width, height } } = chart;
                    const centerX = width / 2;

                    // 💡 반원 도넛 중앙을 살짝 위로
                    let centerY = window.innerWidth <= 576 ? height / 1.55 :
                        window.innerWidth <= 768 ? height / 1.45 :
                            height / 1.35;


                    ctx.save();
                    ctx.beginPath();
                    ctx.arc(centerX, centerY, 50, 0, 2 * Math.PI);
                    ctx.fillStyle = '#ffffff';
                    ctx.fill();
                    ctx.strokeStyle = '#F37221';
                    ctx.lineWidth = 2;
                    ctx.stroke();

                    ctx.font = 'bold 25px sans-serif';
                    ctx.fillStyle = '#000000';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';  // ✅ 정확한 중앙 정렬
                    ctx.fillText(`${percent}%`, centerX, centerY);
                    ctx.restore();
                }
            }]

        });
    });
}

// ⚠ 경고 메시지 표시 함수
function updateRealtimeWarnings(data) {
    datasets.forEach(ds => {
        const elem = document.getElementById(ds.warningId);
        const warning = data[`${ds.msgKey}Warning`];
        const score = data[`${ds.msgKey}Score`] || data[`${ds.msgKey}Percent`] || 0;

        if (!elem) return;

        if (warning === "안정" || !warning) {
            elem.innerHTML = '';
            elem.classList.remove('warning-box', 'blinking');
        } else if (score >= 61) {
            elem.innerHTML = `⚠ ${warning}`;
            elem.classList.add('warning-box', 'blinking');
        }
    });

    // 세부 항목 경고 박스 (압력/온도/용접 등)
    warningDatasets.forEach(ds => {
        const elem = document.getElementById(ds.warningId);
        const warning = data[`${ds.msgKey}Warning`];
        const score = data[`${ds.msgKey}Score`] || 0;

        if (!elem) return;

        if (score >= 61 && warning && warning !== "안정") {
            elem.innerHTML = `⚠ ${warning}`;
            elem.classList.add('warning-box', 'blinking');
        } else {
            elem.innerHTML = '';
            elem.classList.remove('warning-box', 'blinking');
        }
    });
}

// 점검 버튼 및 상태 텍스트 갱신
function updateStatusBoxes(data) {
    datasets.forEach(ds => {
        const statusDiv = document.getElementById(ds.statusId);
        if (!statusDiv) return;

        const statusLabel = statusDiv.querySelector('.defect-label');
        const statusButton = statusDiv.querySelector('.defect-btn');

        // Score 또는 Percent 값 가져오기
        const value = data[`${ds.msgKey}Score`] ?? data[`${ds.msgKey}Percent`] ?? 0;
        const percent = Math.round(value);

        // 텍스트 갱신 (예: 압력/온도 감지 (85%))
        if (statusLabel) {
            statusLabel.textContent = `${ds.label} (${percent}%)`;
        }

        // 버튼 활성화 여부 설정
        if (statusButton) {
            const shouldEnable = percent >= 61;
            statusButton.disabled = !shouldEnable;
            statusButton.onclick = shouldEnable
                ? () => { window.location.href = '/indicators/defect-predict'; }
                : null;
        }
    });
}


// 주석
// ================== 상단 2 : 공정별ok/ng ==================
// function drawChart(ctx, ok, ng, textElementId) {
//     const total = ok + ng;
//     const okPercent = total > 0 ? ((ok / total) * 100).toFixed(0) : 0;
//     const ngPercent = total > 0 ? ((ng / total) * 100).toFixed(0) : 0;
//
//     new Chart(ctx, {
//         type: 'doughnut',
//         data: {
//             labels: ['OK', 'NG'],
//             datasets: [{
//                 data: [ok, ng],
//                 backgroundColor: ['#4CAF50', '#F44336'],
//                 borderWidth: 0
//             }]
//         },
//         options: {
//             responsive: true,
//             maintainAspectRatio: true,
//             cutout: '40%',
//             plugins: {
//                 legend: { display: false }
//             }
//         }
//     });
//
//     // 퍼센트 텍스트 표시
//     const textDiv = document.getElementById(textElementId);
//     textDiv.innerHTML =
//         `<span style="display: inline-block; width: 20px; height: 10px; background-color: #4CAF50; margin-right: 5px;"></span>` +
//         `<span style="font-weight:bold; font-size: 15px">OK </span><span style="font-size: 15px">${ok}건 / ${okPercent}%</span><br>` +
//         `<span style="display: inline-block; width: 20px; height: 10px; background-color: #F44336; margin-right: 5px;"></span>` +
//         `<span style="font-weight:bold; font-size: 15px">NG </span><span style="font-size: 15px">${ng}건 / ${ngPercent}%</span>`;
// }

// 제품 달성률 바 차트의 막대 색상 결정
// function getBarColor(rate) {
//     if (rate < 40) return '#F44336';
//     if (rate < 70) return '#F39C12';
//     if (rate < 100) return '#F1C40F';
//     return '#4CAF50';
// }

// ================== 메인 실행 ==================
// document.addEventListener('DOMContentLoaded', function () {
//
//     // ▶ 1. 공정별 도넛 차트
//     drawChart(document.getElementById('myChart1'), ok1, ng1, 'chartText1');
//     drawChart(document.getElementById('myChart2'), ok2, ng2, 'chartText2');
//     drawChart(document.getElementById('myChart3'), ok3, ng3, 'chartText3');
//
//     // ▶ 1-1. 평균 NG율에 따른 경고 메시지 표시
//     const ngRates = [
//         (ng1 / (ok1 + ng1)) * 100,
//         (ng2 / (ok2 + ng2)) * 100,
//         (ng3 / (ok3 + ng3)) * 100
//     ];
//     const avgNg = ngRates.reduce((a, b) => a + b, 0) / ngRates.length;
//     const warningDiv = document.getElementById('ngWarningMessage');
//
//     let message = '', style = '', icon = '';
//     if (avgNg < 5) {
//         icon = '✅';
//         message = `${icon} 전체 공정 NG율 ${avgNg.toFixed(1)}% 미만 (양호)`;
//         style = 'background-color: #e0f5e9; color: #28a745;';
//     } else if (avgNg < 10) {
//         icon = '🔔';
//         message = `${icon} NG율 ${avgNg.toFixed(1)}% (주의 필요)`;
//         style = 'background-color: #fff9e6; color: #ffc107;';
//         warningDiv.className = 'warning-box blinking'; // 깜박이는 효과
//     } else {
//         icon = '🚨';
//         message = `${icon} 비상! NG율 ${avgNg.toFixed(1)}% 초과`;
//         warningDiv.className = 'warning-box blinking'; // 깜박이는 효과
//     }
//     warningDiv.setAttribute('style', `${style} padding: 5px 10px; border-radius: 10px; font-weight: bold;`);
//     warningDiv.textContent = message;

    // // ▶ 2. 제품 달성률 바 차트
    // const now = new Date();
    // document.getElementById('timeRange').textContent = `${now.getHours()}시 ~ ${now.getHours()}시 59분`;
    //
    // new Chart(document.getElementById('productAchievementChart').getContext('2d'), {
    //     type: 'bar',
    //     data: {
    //         labels: ['GyulRide', 'InteliBike', 'PedalAt4'],
    //         datasets: [{
    //             label: '현재 달성률 (%)',
    //             data: actualRates,
    //             backgroundColor: actualRates.map(getBarColor)
    //         }]
    //     },
    //     options: {
    //         indexAxis: 'y',
    //         responsive: true,
    //         layout: { padding: { right: 120 } },
    //         scales: {
    //             x: {
    //                 beginAtZero: true,
    //                 max: 100,
    //                 ticks: {
    //                     stepSize: 20,
    //                     font: { size: 14 },
    //                     callback: (val) => `${val}%`
    //                 }
    //             },
    //             y: {
    //                 ticks: { font: { size: 16 } }
    //             }
    //         },
    //         plugins: {
    //             legend: { display: false },
    //             tooltip: {
    //                 callbacks: {
    //                     label: (ctx) => {
    //                         const i = ctx.dataIndex;
    //                         return `${actualCounts[i]}/${targets[i]}대 (${actualRates[i].toFixed(1)}%)`;
    //                     }
    //                 }
    //             }
    //         }
    //     },
    //     plugins: [{
    //         id: 'barLabelPlugin',
    //         afterDatasetsDraw(chart) {
    //             const { ctx, scales: { x, y } } = chart;
    //             chart.data.datasets[0].data.forEach((percent, i) => {
    //                 const label = `${actualCounts[i]}/${targets[i]}대`;
    //                 ctx.save();
    //                 ctx.font = 'bold 16px sans-serif';
    //                 ctx.fillStyle = percent < 60 ? '#F44336' : '#333';
    //                 ctx.textAlign = 'left';
    //                 ctx.fillText(label, x.getPixelForValue(100) + 10, y.getPixelForValue(i));
    //                 ctx.restore();
    //             });
    //         }
    //     }]
    // });


