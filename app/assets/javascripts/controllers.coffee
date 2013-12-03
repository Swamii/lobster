'use strict'

app = angular.module 'app.controllers', []

app.controller 'DudeCtrl', ['$scope', 'Api', 'dudes', ($scope, Api, dudes) ->
  $scope.dudes = dudes
  $scope.newDude = {}
  $scope.reversed = false

  $scope.toggleReversed = ->
    $scope.reversed = !$scope.reversed

  $scope.processForm = ->
    Api.all('dude').post($scope.newDude).then (dude) ->
      $scope.dudes.push dude
      $scope.newDude = {}
    , () ->
      console.log "error error"

  $scope.selectDude = (selectedDude) ->
    angular.forEach $scope.dudes, (dude) ->
      dude.selected = dude is selectedDude

]

app.controller 'DudeDetailCtrl', ['$scope', 'Api', 'dudes', 'dude', ($scope, Api, dudes, selectedDude) ->
  $scope.dude = selectedDude

  closeEdit = ->
    $scope.editDude = {}
    $scope.isEdit = false

  closeEdit()
  $scope.closeEdit = closeEdit

  getIndex = (dude) ->
    idx = -1
    angular.forEach dudes, (d, cIdx) ->
      if dude.id is d.id
        idx = cIdx
    return idx

  $scope.deleteDude = ->
    selectedDude.remove().then ->
      idx = getIndex selectedDude
      dudes.splice idx, 1
      $scope.$state.go 'dude'

  $scope.openForEdit = ->
    $scope.isEdit = true
    $scope.editDude = Api.copy $scope.dude

  $scope.updateDude = ->
    editDude = $scope.editDude
    editDude.put().then (dude) ->
      idx = getIndex dude
      dudes.splice idx, 1, dude
      $scope.dude = dude
      closeEdit()
    , ->
      console.log "error error putting"
]