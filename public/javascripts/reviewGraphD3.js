var data = [];
var graphHeight = 100;
var graphMargin = 25;
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
var overallGraphBorderDetails = '';//1px black solid';
var xscale = d3.scale.linear().domain([0, 10]).range([0, graphWidth - (graphMargin * 2)]);
var xaxis = d3.svg.axis().scale(xscale).orient('bottom');

function start(data) {
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
    var x = 0;
    if (d.peerScores != undefined) x = d3.max(d.peerScores) - d3.min(d.peerScores);
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
    var x = 0;
    if (d.peerScores != undefined) x = d3.mean(d.peerScores) - d3.min(d.peerScores);
    if (x == 0) return 'translate(0, 0)';
    else return 'translate(' + (x * peerScoresMaxWidth / maxRange) + ', 0)';
  });

  function getPeerText(d,i) {
    if (d.peerScores == undefined)
      return "No Peer Scores";
    return "Peer Score Range";
  }

  peerGroup
  .attr('transform', function(d, i) {
    var peerScoresMin = 0;
    if (d.peerScores != undefined)
      peerScoresMin = d3.min(d.peerScores);
    var x = ((peerScoresMin * peerScoresMaxWidth / maxRange) + peerScoresMargin);
    var y = ((graphHeight - peerScoresHeight) / 4);
    return 'translate(' + x + ',' + y + ')'
  })
  .append('text')
  .text(getPeerText)
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + (peerScoresHeight - peerScoresMargin + labelOffset/2) + ')');

  var selfTranslateFunction = function(d, i) {
    var selfScore = 0;
    if (d.selfScore != undefined)
      selfScore = d.selfScore;
    var x = ((selfScore * peerScoresMaxWidth / maxRange) + (graphMargin));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 2);
    return 'translate(' + x + ',' + y + ')';
  };

  var selfGroup = graphs.append('g')
  .attr('transform', selfTranslateFunction);
  selfGroup
  .append('circle')
  .attr('r', markerDimension)
  .attr('cx', markerDimension/4)
  .attr('cy', markerDimension/2)
  .style('fill', selfColor);

  function getSelfText(d,i) {
    if (d.selfScore == undefined)
      return "No Self Score";
    return "Self";
  }

  selfGroup.append('text')
  .text(getSelfText)
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + labelOffset + ')');

  var managerTranslateFunction = function(d, i) {
    var mgrScore = 0;
    if (d.managerScore != undefined)
      mgrScore = d.managerScore;
    var x = ((mgrScore * peerScoresMaxWidth / maxRange) + (graphMargin));
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

  function getManagerText(d,i) {
    if (d.managerScore == undefined)
      return "No Mgr Score";
    return "Mgr";
  }

  managerGroup.append('text')
  .text(getManagerText)
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + labelOffset + ')');

}
