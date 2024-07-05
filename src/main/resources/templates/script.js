// Function to fetch data from the API
async function fetchData(apiUrl) {
  try {
    const response = await fetch(apiUrl);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching data:', error);
    return null;
  }
}

// Replace this URL with the actual API endpoint
const apiUrl = 'http://localhost:8080/api/meters/process';

async function initializeCharts() {
  const data = await fetchData(apiUrl);

  if (!data) {
    console.error('Failed to fetch data');
    return;
  }

  const meterData = data.meterData;
  const volumeData = data.volumeData;
  const consumptionProduction = data.consumptionProduction;

  // Function to format date from timestamp
  function formatDate(timestamp) {
    var date = new Date(timestamp);
    var year = date.getFullYear();
    var month = (date.getMonth() + 1).toString().padStart(2, '0');
    var day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Formatting data for consumption/production chart
  var consumptionProductionData = {
    labels: consumptionProduction.map(entry => formatDate(entry.ts)),
    datasets: [{
      label: 'Consumption',
      data: consumptionProduction.map(entry => entry.consumption),
      backgroundColor: 'rgba(255, 99, 132, 0.2)',
      borderColor: 'rgba(255, 99, 132, 1)',
      borderWidth: 1,
      fill: true
    },
      {
        label: 'Production',
        data: consumptionProduction.map(entry => entry.production),
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
        fill: true
      }
    ]
  };

  var consumptionProductionData2 = {
    labels: consumptionProduction.map(entry => formatDate(entry.ts)),
    datasets: [{
      label: 'Consumption',
      data: consumptionProduction.map(entry => entry.consumption),
      backgroundColor: 'rgba(20, 200, 132, 0.2)',
      borderColor: 'rgba(20, 200, 132, 1)',
      borderWidth: 1,
      fill: true
    },
      {
        label: 'Production',
        data: consumptionProduction.map(entry => entry.production),
        backgroundColor: 'rgba(70, 100, 220, 0.2)',
        borderColor: 'rgba(70, 100, 220, 1)',
        borderWidth: 1,
        fill: true
      }
    ]
  };

  var meterDataSets = [];
  meterData.forEach(sensor => {
    var data = {
      label: `Sensor ID ${sensor.sensorId}`,
      data: sensor.data.map(entry => parseFloat(entry.value)),
      backgroundColor: sensor.sensorId === 'ID735' ? 'rgba(54, 162, 235, 0.2)' : 'rgba(255, 99, 132, 0.2)',
      borderColor: sensor.sensorId === 'ID735' ? 'rgba(54, 162, 235, 1)' : 'rgba(255, 99, 132, 1)',
      borderWidth: 1,
      fill: true
    };
    meterDataSets.push(data);
  });

// Create labels for the X-axis using the timestamps
  var meterDataLine = {
    labels: meterData[0].data.map(entry => new Date(entry.ts).toLocaleDateString()), // Format the timestamp to a readable date format
    datasets: meterDataSets
  };

  // Initializing the charts
  var ctx = document.getElementById('myChart').getContext('2d');
  var ctxSecond = document.getElementById('mySecondChart').getContext('2d');
  var ctxBarChart = document.getElementById('myBarChart').getContext('2d');

  var myChart = new Chart(ctx, {
    type: 'line',
    data: meterDataLine,
    options: {
      scales: {
        y: {
          beginAtZero: true
        },
        x: {
          type: 'category',
          labels: meterDataLine.labels,
          title: {
            display: true,
          }
        }
      },
      plugins: {
        legend: {
          display: false,
          position: 'top'
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

  var myBarChart = new Chart(ctxBarChart, {
    type: 'line',
    data: consumptionProductionData2,
    options: {
      scales: {
        y: {
          beginAtZero: true
        },
        x: {
          type: 'category',
          labels: consumptionProductionData2.labels,
          title: {
            display: true,
          }
        }
      },
      plugins: {
        legend: {
          display: false,
          position: 'top'
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

  var mySecondChart = new Chart(ctxSecond, {
    type: 'bar',
    data: {
      labels: volumeData.volumeData.map(entry => formatDate(entry.ts)),
      datasets: volumeData.volumeData[0].data.map(obis => {
        let rot = Math.floor(Math.random() * 256);
        let gruen = Math.floor(Math.random() * 256);
        let blau = Math.floor(Math.random() * 256);
        return {
          label: obis.obis,
          data: volumeData.map(entry => parseFloat(entry.data.find(d => d.obis === obis.obis).value)),
          backgroundColor: `rgba(${rot}, ${gruen}, ${blau}, 0.2)`,
          borderColor: `rgba(${rot}, ${gruen}, ${blau}, 1)`,
          borderWidth: 1
        };
      })
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
          display: false,
          position: 'top'
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

  // Adding event listeners to switch charts
  document.getElementById('prevChart').addEventListener('click', function() {
    document.getElementById('myChart').style.display = 'block';
    document.getElementById('mySecondChart').style.display = 'none';
  });

  document.getElementById('nextChart').addEventListener('click', function() {
    document.getElementById('myChart').style.display = 'none';
    document.getElementById('mySecondChart').style.display = 'block';
  });

  // Reset zoom buttons
  document.getElementById('resetZoomLine').addEventListener('click', function() {
    myChart.resetZoom();
    mySecondChart.resetZoom();
  });

  document.getElementById('resetZoomBar').addEventListener('click', function() {
    myBarChart.resetZoom();
  });

  // Define a variable to keep track of the current chart index
  var currentChartIndex = 0;
  var charts = [document.getElementById('myChart'), document.getElementById('mySecondChart')];

  // Function to update chart visibility and indicator
  function updateChartDisplay() {
    charts.forEach((chart, index) => {
      chart.style.display = index === currentChartIndex ? 'block' : 'none';
    });
    document.getElementById('chartIndicator').innerText = `Diagramm ${currentChartIndex + 1} von ${charts.length}`;
  }

  // Event listeners to switch charts and update indicator
  document.getElementById('prevChart').addEventListener('click', function() {
    currentChartIndex = (currentChartIndex - 1 + charts.length) % charts.length;
    updateChartDisplay();
  });

  document.getElementById('nextChart').addEventListener('click', function() {
    currentChartIndex = (currentChartIndex + 1) % charts.length;
    updateChartDisplay();
  });

  // Initial display update
  updateChartDisplay();

  // Function to toggle dataset visibility
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
          toggleDatasetVisibility(myBarChart, sellerName, visibility);
        } else if (chartType === 'bar') {
          toggleDatasetVisibility(mySecondChart, sellerName, visibility);
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
    var sellerListLine2 = document.getElementById('sellerListLine2');
    sellerListLine.innerHTML = '';
    sellerListBar.innerHTML = '';
    sellerListLine2.innerHTML = '';

    var sellers = [];

    sellers = sellers.concat(extractSellersFromCharts(myChart));
    sellers = sellers.concat(extractSellersFromCharts(mySecondChart));
    sellers = sellers.concat(extractSellersFromCharts(myBarChart));

    var chartColors = {
      myChart: getDatasetColors(myChart),
      mySecondChart: getDatasetColors(mySecondChart),
      myBarChart: getDatasetColors(myBarChart)
    };

    sellers.forEach(function(seller) {
      var sellerItem = document.createElement('div');
      sellerItem.className = 'list-group-item';

      var colorSquare = document.createElement('div');
      colorSquare.className = 'color-square visible';
      colorSquare.style.backgroundColor = chartColors.myChart[seller] || chartColors.mySecondChart[seller] || chartColors.myBarChart[seller];
      colorSquare.setAttribute('data-seller', seller);

      if (chartColors.myChart[seller]) {
        colorSquare.setAttribute('data-chart', 'line');
        sellerListLine.appendChild(sellerItem);
      } else if(chartColors.mySecondChart[seller]){
        colorSquare.setAttribute('data-chart', 'bar');
        sellerListBar.appendChild(sellerItem);
      } else {
        colorSquare.setAttribute('data-chart', 'line');
        sellerListLine2.appendChild(sellerItem);
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


  document.addEventListener("DOMContentLoaded", function() {
    var chartContainers = document.querySelectorAll('.chart-container');

    // Funktion zum Ein- und Ausblenden des Chart-Indicators mit Fade-Effekt
    function toggleChartIndicator(chartContainer) {
      var chartIndicator = chartContainer.querySelector('#chartIndicator');

      // Zeige den Chart-Indicator an und animiere die Opazität
      chartIndicator.style.opacity = 1;

      // Timeout zum Ausblenden des Chart-Indicators nach 2 Sekunden
      setTimeout(function() {
        chartIndicator.style.opacity = 0;
      }, 2000); // 2 Sekunden Timeout
    }

    // Event Listener für Klick auf "Vorheriges" und "Nächstes" Diagramm-Buttons
    chartContainers.forEach(function(chartContainer) {
      var prevButton = chartContainer.querySelector('#prevChart');
      var nextButton = chartContainer.querySelector('#nextChart');

      prevButton.addEventListener('click', function() {
        toggleChartIndicator(chartContainer);
      });

      nextButton.addEventListener('click', function() {
        toggleChartIndicator(chartContainer);
      });
    });
  });

  /*document.addEventListener("DOMContentLoaded", function() {
    var chartContainers = document.querySelectorAll('.chart-container');

    // Funktion zum Ein- und Ausblenden des Chart-Indicators
    function toggleChartIndicator(chartContainer) {
        var chartIndicator = chartContainer.querySelector('#chartIndicator');

        chartContainer.classList.add('fade-out'); // Verblassen der Umrandung
        chartIndicator.style.display = 'block'; // Chart-Indicator anzeigen

        // Timeout zum Ausblenden des Chart-Indicators nach 2 Sekunden
        setTimeout(function() {
            chartIndicator.style.display = 'none';
            chartContainer.classList.remove('fade-out'); // Entfernen des Verblassungseffekts
        }, 2000); // 2 Sekunden Timeout
    }

    // Event Listener für Klick auf "Vorheriges" und "Nächstes" Diagramm-Buttons
    chartContainers.forEach(function(chartContainer) {
        var prevButton = chartContainer.querySelector('#prevChart');
        var nextButton = chartContainer.querySelector('#nextChart');

        prevButton.addEventListener('click', function() {
            toggleChartIndicator(chartContainer);
        });

        nextButton.addEventListener('click', function() {
            toggleChartIndicator(chartContainer);
        });
    });
  });*/

}

// Initialize charts on window load
window.addEventListener('load', initializeCharts);
  