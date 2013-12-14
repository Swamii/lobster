'use strict';

module.exports = function(grunt) {
	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),
		coffee: {
			files: {
				src: ['public/app/**/*.coffee'],
				dest: 'public/javascripts/script.js'
			}
		},
		watch: {
			files: ['public/app/**/*.coffee'],
			tasks: 'default'
		}
	});

    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-coffee');

    grunt.registerTask('default', ['coffee']);

};