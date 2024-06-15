var ctx = document.getElementById('myChart').getContext('2d');

var labels = [];
for (var i = 0; i < 10; i++) { 
    labels.push((i * 15) + ' min'); 
}

var data = {
    labels: labels,
    datasets: [{
            label: 'Verkäufe',
            data: [1, 7, 8, 15, 20, 23, 40, 42, 50, 60], 
            backgroundColor: 'rgba(70, 162, 235, 0.2)',
            borderColor: 'rgba(70, 162, 235, 1)',
            borderWidth: 1,
            fill: true
        },
        {
            label: 'Verkäufe1',
            data: [6, 9, 12, 18, 21, 25, 31, 49, 55, 65], 
            backgroundColor: 'rgba(0, 0, 255, 0.2)',
            borderColor: 'rgba(0, 0, 255, 1)',
            borderWidth: 1,
            fill: true
        }
    ]
};

var myChart = new Chart(ctx, {
    type: 'line',
    data: data,
    options: {
        scales: {
            y: {
                beginAtZero: true
            }
        },
        plugins: {
        legend: {
            display: false
        },
        zoom: {
            pan: {
            enabled: true,
            mode: 'xy',
            threshold: 5,
            },
            zoom: {
            wheel: {
                enabled: true
            },
            pinch: {
                enabled: true
            },
            mode: 'xy',
            },
        }
        }
    }
});

function lastFiveDates() {
    var lastTenTable = document.getElementById('lastTenTable').getElementsByTagName('tbody')[0];
    lastTenTable.innerHTML = ''; 

    for (var i = 0; i < 10; i++) {
    var row = lastTenTable.insertRow();
    var cell1 = row.insertCell(0);
    var cell2 = row.insertCell(1);
    var cell3 = row.insertCell(2);

    cell1.textContent = data.labels[data.labels.length - 1 - i]; 
    cell2.textContent = data.datasets[0].data[data.labels.length - 1 - i];
    cell3.textContent = data.datasets[1].data[data.labels.length - 1 - i];
    }
}

lastFiveDates();

document.getElementById('resetZoomLine').addEventListener('click', function() {
    myChart.resetZoom();
});

var ctxBar = document.getElementById('myBarChart').getContext('2d');
var myBarChart = new Chart(ctxBar, {
    type: 'bar',
    data: {
        labels: ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli'],
        datasets: [
            {
                label: 'Verkäufe 2',
                data: [-12, -19, -3, -5, -2, -3, -9],
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            },
            {
                label: 'Verkäufe 3',
                data: [8, 11, 7, 2, 5, 1, 4],
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            },
            {
                label: 'Verkäufe 4',
                data: [-4, -6, -8, -11, -3, -6, -7],
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            },
            {
                label: 'Verkäufe 5',
                data: [7, 3, 5, 2, 6, 4, 2],
                backgroundColor: 'rgba(255, 206, 86, 0.2)',
                borderColor: 'rgba(255, 206, 86, 1)',
                borderWidth: 1
            }
        ]
    },
    options: {
    scales: {
        y: {
            beginAtZero: true
        },
        x: {
            beginAtZero: true,
            barPercentage: 1.0,
            categoryPercentage: 1.0
        }
    },
    plugins: {
        legend: {
        display: false
        },
        zoom: {
        pan: {
            enabled: true,
            mode: 'xy',
            threshold: 5,
        },
        zoom: {
            wheel: {
            enabled: true
            },
            pinch: {
            enabled: true
            },
            mode: 'xy',
        },
        }
    }
    }
});

document.getElementById('resetZoomBar').addEventListener('click', function() {
    myBarChart.resetZoom();
});

function toggleDatasetVisibility(chart, label, visibility) {
    if (chart && chart.data && chart.data.datasets) {
        chart.data.datasets.forEach(function(dataset) {
            if (dataset.label === label) {
                dataset.hidden = !visibility;
            }
        });
        chart.update(); 
    }
}

function setupColorSquareListeners() {
    var colorSquares = document.querySelectorAll('.color-square');

    colorSquares.forEach(function(square) {
        square.addEventListener('click', function() {
            var sellerName = square.getAttribute('data-seller');
            var chartType = square.getAttribute('data-chart');

            var visibility = square.classList.contains('disabled');
            if (chartType === 'line') {
                toggleDatasetVisibility(myChart, sellerName, visibility);
            } else if (chartType === 'bar') {
                toggleDatasetVisibility(myBarChart, sellerName, visibility);
            }

            square.classList.toggle('disabled');
            square.classList.toggle('visible', !square.classList.contains('disabled'));
        });
    });

    var listGroupItems = document.querySelectorAll('.list-group-item');

    listGroupItems.forEach(function(item) {
        item.addEventListener('click', function() {
            var square = item.querySelector('.color-square');
            square.click(); 
        });
    });
}

function updateSellerList() {
    var sellerListLine = document.getElementById('sellerListLine');
    var sellerListBar = document.getElementById('sellerListBar');
    sellerListLine.innerHTML = ''; 
    sellerListBar.innerHTML = ''; 

    var sellers = [];

    sellers = sellers.concat(extractSellersFromCharts(myChart));
    sellers = sellers.concat(extractSellersFromCharts(myBarChart));

    var chartColors = {
        myChart: getDatasetColors(myChart),
        myBarChart: getDatasetColors(myBarChart)
    };

    sellers.forEach(function(seller) {
        var sellerItem = document.createElement('div');
        sellerItem.className = 'list-group-item';

        var colorSquare = document.createElement('div');
        colorSquare.className = 'color-square visible'; 
        colorSquare.style.backgroundColor = chartColors.myChart[seller] || chartColors.myBarChart[seller];
        colorSquare.setAttribute('data-seller', seller);

        if (chartColors.myChart[seller]) {
            colorSquare.setAttribute('data-chart', 'line');
            sellerListLine.appendChild(sellerItem);
        } else {
            colorSquare.setAttribute('data-chart', 'bar');
            sellerListBar.appendChild(sellerItem);
        }

        var label = document.createElement('span');
        label.textContent = seller;

        sellerItem.appendChild(colorSquare);
        sellerItem.appendChild(label);
    });

    setupColorSquareListeners();
}

updateSellerList();

function extractSellersFromCharts(chart) {
    var sellers = [];
    if (chart && chart.data && chart.data.datasets) {
        chart.data.datasets.forEach(function(dataset) {
            if (!sellers.includes(dataset.label)) {
                sellers.push(dataset.label);
            }
        });
    }
    return sellers;
}

function getDatasetColors(chart) {
    var colors = {};
    if (chart && chart.data && chart.data.datasets) {
        chart.data.datasets.forEach(function(dataset) {
            colors[dataset.label] = dataset.backgroundColor || dataset.borderColor || 'black';
        });
    }
    return colors;
}


document.getElementById('exportButton').addEventListener('click', function() {
    alert('Export-Funktion wird ausgeführt');
});

document.getElementById('saveButton').addEventListener('click', function() {
    alert('Save-Funktion wird ausgeführt');
});