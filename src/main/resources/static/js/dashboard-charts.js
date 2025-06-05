// 대시보드 추가할 차트들 모아놓고
// <th:block layout:fragment="script">
//     <script src="/js/dashboard-charts.js"></script>
// </th:block> 를 이용하여 집어넣기

// 상단 우측 :  공정별 현재 상태  (by. ProcessList.html)
function drawChart(ctx, ok, ng, textElementId) {
    const total = ok + ng;
    const okPercent = total > 0 ? ((ok / total) * 100).toFixed(0) : 0;
    const ngPercent = total > 0 ? ((ng / total) * 100).toFixed(0) : 0;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['OK', 'NG'],
            datasets: [{
                data: [ok, ng],
                backgroundColor: ['#4CAF50', '#F44336'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            cutout: '40%',
            plugins: {
                legend: { display: false }
            }
        }
    });

    const textDiv = document.getElementById(textElementId);
    textDiv.innerHTML =
        `<span style="display: inline-block; width: 20px; height: 10px; background-color: #4CAF50; margin-right: 5px;"></span>` +
        `<span style="font-weight:bold; font-size: 15px">OK </span><span style="font-size: 15px">${ok}건 / ${okPercent}%</span><br>` +
        `<span style="display: inline-block; width: 20px; height: 10px; background-color: #F44336; margin-right: 5px;"></span>` +
        `<span style="font-weight:bold; font-size: 15px">NG </span><span style="font-size: 15px">${ng}건 / ${ngPercent}%</span>`;
}

document.addEventListener('DOMContentLoaded', function () {
    drawChart(document.getElementById('myChart1'), 90, 10, 'chartText1'); // 각 공정 차트 그리기
    drawChart(document.getElementById('myChart2'), 90, 10, 'chartText2');
    drawChart(document.getElementById('myChart3'), 90, 10, 'chartText3');

    const ngRates = [ // NG율 평균 계산
        (30 / (70 + 30)) * 100,
        (70 / (30 + 70)) * 100,
        (55 / (45 + 55)) * 100
    ];
    const avgNg = ngRates.reduce((sum, val) => sum + val, 0) / ngRates.length;

    const warningDiv = document.getElementById('ngWarningMessage');

    let message = '';
    let style = '';
    let icon = '';

    if (avgNg < 10) {
        icon = '✅';
        message = `${icon} 전체 공정 NG율 ${avgNg.toFixed(1)}% 미만 (양호)`;
        style = 'background-color: #e0f5e9; color: #28a745; padding: 5px 10px; border-radius: 10px; font-weight: bold;';
        warningDiv.className = 'warning-placeholder';
        warningDiv.setAttribute('style', style);
        warningDiv.textContent = message;
    } else if (avgNg >= 10 && avgNg < 20) {
        icon = '🔔';
        message = `${icon} 전체 공정 NG율 ${avgNg.toFixed(1)}% (주의 필요)`;
        style = 'background-color: #fff9e6; color: #ffc107; padding: 5px 10px; border-radius: 10px; font-weight: bold;';
        warningDiv.className = 'warning-placeholder';
        warningDiv.setAttribute('style', style);
        warningDiv.textContent = message;
    } else if (avgNg >= 20 && avgNg < 30) {
        icon = '⚠';
        message = `${icon} 심각! NG율 ${avgNg.toFixed(1)}% (라인 점검 필요!)`;
        style = 'background-color: #ffe6f0; color: #ff4d4d; padding: 5px 10px; border-radius: 10px; font-weight: bold;';
        warningDiv.className = 'warning-placeholder';
        warningDiv.setAttribute('style', style);
        warningDiv.textContent = message;
    } else if (avgNg >= 30) {
        icon = '🚨';
        message = `${icon} 비상! 전체 공정 NG율 ${avgNg.toFixed(1)}% 초과 (공정 중단 필요!)`;
        warningDiv.className = 'warning-box blinking';
        warningDiv.textContent = message;
    } else {
        message = `전체 공정 NG율 ${avgNg.toFixed(1)}%`;
        warningDiv.className = 'warning-placeholder';
        warningDiv.textContent = message;
    }

    // 2. 상단 좌측 : 현재 시간 목표량 차트
    const targets = [100, 100, 100];
    const actualCounts = [15, 60, 100];
    const actualRates = actualCounts.map((count, i) => (count / targets[i]) * 100);

    function getBarColor(rate) {
        if (rate < 40) return '#F44336';
        if (rate < 70) return '#F39C12';
        if (rate < 100) return '#F1C40F';
        return '#4CAF50';
    }

    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();
    document.getElementById('timeRange').textContent = `${currentHour}시 ~ ${currentHour}시 59분`;

    new Chart(document.getElementById('productAchievementChart').getContext('2d'), {
        type: 'bar',
        data: {
            labels: ['GyulRide', 'InteliBike', 'PedalAt4'],
            datasets: [{
                label: '현재 달성률 (%)',
                data: actualRates,
                backgroundColor: actualRates.map(getBarColor)
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            layout: {
                padding: { right: 120 }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    max: 100,
                    ticks: {
                        stepSize: 20,
                        font: { size: 14 },
                        callback: (value) => value + '%'
                    }
                },
                y: {
                    ticks: { font: { size: 16 } }
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            const i = context.dataIndex;
                            const rate = actualRates[i].toFixed(1);
                            const actual = actualCounts[i];
                            const target = targets[i];
                            return `${actual}/${target}대 (${rate}%)`;
                        }
                    }
                }
            }
        },
        plugins: [{
            id: 'barLabelPlugin',
            afterDatasetsDraw(chart) {
                const { ctx, scales: { x, y } } = chart;
                chart.data.datasets[0].data.forEach((percent, i) => {
                    const actual = actualCounts[i];
                    const target = targets[i];
                    const label = `${actual}/${target}대`;
                    const yPos = y.getPixelForValue(i);
                    const xPos = x.getPixelForValue(100) + 10;
                    const fillColor = percent < 60 ? '#F44336' : '#333';

                    ctx.save();
                    ctx.font = 'bold 16px sans-serif';
                    ctx.fillStyle = fillColor;
                    ctx.textAlign = 'left';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(label, xPos, yPos);
                    ctx.restore();
                });
            }
        }]
    });

    // 하단 :  기계 이상 감지 (from. defectLog.html)
    const datasets = [
        { id: 'castingChart', statusId: 'castingStatus', warningId: 'castingWarning', label: '압력/온도 감지', value: 80 },
        { id: 'weldingChart', statusId: 'weldingStatus', warningId: 'weldingWarning', label: '용접 출력 감지', value: 20 }
    ];

    datasets.forEach(data => {
        const canvas = document.getElementById(data.id);
        const ctx = canvas.getContext('2d');
        canvas.width = canvas.parentNode.offsetWidth;

        let bgColor = data.value <= 60 ? 'green' : (data.value <= 79 ? '#FFD700' : 'red');

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
                afterDraw: chart => {
                    const { ctx, chartArea: { width, height } } = chart;
                    const centerX = width / 2;
                    const centerY = height / 1.3;
                    const smallRadius = 50;

                    ctx.save();
                    ctx.beginPath();
                    ctx.arc(centerX, centerY, smallRadius, 0, 2 * Math.PI);
                    ctx.fillStyle = '#ffffff';
                    ctx.fill();
                    ctx.strokeStyle = '#F37221';
                    ctx.lineWidth = 1;
                    ctx.stroke();
                    ctx.closePath();

                    ctx.font = '15px sans-serif';
                    ctx.fillStyle = 'black';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(`${data.value}%`, centerX, centerY);
                    ctx.restore();
                }
            }]
        });

        const statusDiv = document.getElementById(data.statusId);
        const statusLabel = statusDiv.querySelector('.defect-label');
        const statusButton = statusDiv.querySelector('.defect-btn');

        const isWarning = data.value >= 80;
        statusButton.disabled = !isWarning;
        statusButton.onclick = isWarning ? () => {
            window.location.href = '/indicators/defect-predict';
        } : null;

        statusLabel.textContent = `${data.label} (${data.value}%)`;

        const warningDiv = document.getElementById(data.warningId);
        if (isWarning) {
            warningDiv.className = 'warning-box blinking warning-placeholder';
            warningDiv.innerHTML = `⚠ 주의! ${data.label} 초과 경고`;
        } else {
            warningDiv.className = 'warning-placeholder';
            warningDiv.innerHTML = '';
        }
    });
});
