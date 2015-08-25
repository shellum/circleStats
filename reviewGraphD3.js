var data = [];
var graphHeight = 100;
var graphMargin = 10;
var graphWidth = 400;
var overallGraphsWidth = 500;
var overallGraphsHeight = 0;
var peerScoresHeight = 20;
var peerScoresMargin = 10;
var axisOffset = 25;
var peerScoresMaxWidth = graphWidth-(2*peerScoresMargin);
var maxRange = 10;
var markerDimension = 6;
var infitesimalDelta = 5;
var titleXOffset = 10;
var titleYOffset = 15;
var labelSize = 12;
var labelOffset = 8;
var selfColor = 'green';
var managerColor = 'purple';
var fontFamily = 'arial';
var graphBackgroundColor = '#eeeeee';
var averagePeerColor = 'white';
var peerGradientStartEndColor = 'lightblue';
var overallGraphBorderDetails = '1px black solid';
var xscale = d3.scale.linear().domain([0, 10]).range([0, graphWidth - (graphMargin * 2)]);
var xaxis = d3.svg.axis().scale(xscale).orient('bottom');
$(function() {
  data = [
    {id: 1, title: 'Awesomeness', peerScores: [0,1,3,3,3,3,3,6,7,10], selfScore: 9, managerScore: 10},
    {id: 2, title: 'Creative', peerScores: [3,3], selfScore: 7, managerScore: 6},
    {id: 3, title: 'Helpful', peerScores: [0,0,0,0,5], selfScore: 5, managerScore: 8},
    {id: 4, title: 'Challenging', peerScores: [4,5,5], selfScore: 2, managerScore: 3}
  ];

  start();
});
function start() {
  overallGraphsHeight = (graphHeight + graphMargin) * data.length + graphMargin;

  d3.select('svg')
  .style('height', overallGraphsHeight)
  .style('width', overallGraphsWidth)
  .style('border', overallGraphBorderDetails);

  var group = d3.select('svg')
  .selectAll('rect')
  .call(xaxis)
  .data(data)
  .enter()
  .append('g')
  .attr('transform', function(d, i) {return 'translate(' + (graphMargin) + ', ' + (((i + 1) * graphMargin) + i * graphHeight) + ')';})
  .append('rect')
  .attr('height', graphHeight)
  .attr('width', graphWidth)
  .style('fill',graphBackgroundColor)
  .style('margin',graphMargin);

  var graphs = d3.selectAll('g');

  graphs
  .append('text')
  .attr('x', titleXOffset)
  .attr('y', titleYOffset)
  .attr('font-family', fontFamily)
  .text(function(d, i) {
    return d.title;
  });

  d3.select('svg')
  .selectAll('g')
  .append('g')
  .attr('transform', 'translate(' + graphMargin + ',' + (graphHeight - axisOffset) + ')')
  .attr('font-family', fontFamily)
  .attr('font-size', labelSize)
  .call(xaxis);

  var peerGroup = graphs.append('g');
  peerGroup
  .append('rect')
  .attr('height', peerScoresHeight)
  .attr('width', function(d, i) {
    var x = (d3.max(d.peerScores) - d3.min(d.peerScores));
    if (x == 0) return infitesimalDelta;
    else return x * peerScoresMaxWidth / maxRange;
  } )
  .style('fill', peerGradientStartEndColor);

  peerGroup
  .append('rect')
  .attr('height', peerScoresHeight)
  .attr('width', infitesimalDelta)
  .style('fill', averagePeerColor)
  .attr('transform', function(d, i) {
    var x = d3.mean(d.peerScores) - d3.min(d.peerScores);
    if (x == 0) return 'translate(0, 0)';
    else return 'translate(' + (x * peerScoresMaxWidth / maxRange) + ', 0)';
  });

  peerGroup
  .attr('transform', function(d, i) {
    var x = ((d3.min(d.peerScores) * peerScoresMaxWidth / maxRange) + peerScoresMargin);
    var y = ((graphHeight - peerScoresHeight) / 4);
    return 'translate(' + x + ',' + y + ')'
  })
  .append('text')
  .text('Peer Score Range')
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + (peerScoresHeight - peerScoresMargin + labelOffset/2) + ')');

  var selfTranslateFunction = function(d, i) {
    var x = ((d.selfScore * peerScoresMaxWidth / maxRange) + (markerDimension / 2));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 2);
    return 'translate(' + x + ',' + y + ')'
  };

  var selfGroup = graphs.append('g')
  .attr('transform', selfTranslateFunction);
  selfGroup
  .append('circle')
  .attr('r', markerDimension)
  .attr('cx', markerDimension/4)
  .attr('cy', markerDimension/2)
  .style('fill', selfColor);

  selfGroup.append('text')
  .text('Self')
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + labelOffset + ')');

  var managerTranslateFunction = function(d, i) {
    var x = ((d.managerScore * peerScoresMaxWidth / maxRange) + (markerDimension / 2));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 4);
    return 'translate(' + x + ',' + y + ')'
  };

  var managerGroup = graphs.append('g')
  .attr('transform', managerTranslateFunction)
  managerGroup
  .append('circle')
  .attr('r', markerDimension)
  .attr('cx', markerDimension/4)
  .attr('cy', markerDimension/2)
  .style('fill', managerColor);

  managerGroup.append('text')
  .text('Mgr')
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + labelOffset + ')');

}
