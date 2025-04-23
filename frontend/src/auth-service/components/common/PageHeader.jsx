import React from 'react';
import PropTypes from 'prop-types';

const PageHeader = ({ text }) => {
  return (
    <div className="page-header">
      <h1>{text}</h1>
    </div>
  );
};

PageHeader.propTypes = {
  text: PropTypes.string.isRequired,
};

export default PageHeader;