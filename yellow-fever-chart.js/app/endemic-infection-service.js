var promise;
var day = [];
var susceptible = [];
var exposed = [];
var mildInfection = [];
var severeInfection = [];
var toxicInfection = [];
var recovered = [];

// join data human health
promise = d3.csv('data/scenario-endemic-infection/1/human-health.csv');
promise.then(function(result) {
  result.filter(function(line, index) {
    var values = [];
    values.push(line.SUSCEPTIBLE);
    susceptible.push(values);
  });
}, function(error) {});

promise = d3.csv('data/scenario-endemic-infection/2/human-health.csv');
promise.then(function(result) {
  result.filter(function(line, index) {
    susceptible[index].push(line.SUSCEPTIBLE);
  });
}, function(error) {});

promise = d3.csv('data/scenario-endemic-infection/3/human-health.csv');
promise.then(function(result) {
  result.filter(function(line, index) {
    susceptible[index].push(line.SUSCEPTIBLE);
  });
}, function(error) {});

d3.csv("data/scenario-endemic-infection/3/human-health.csv", function(line) {
  var values = [];
  values.push(line.SUSCEPTIBLE);
  susceptible.push(values);
});

// calculate standard deviation
susceptible.filter(function(line) {
  console.log(line);
});
// generate chart to human health
